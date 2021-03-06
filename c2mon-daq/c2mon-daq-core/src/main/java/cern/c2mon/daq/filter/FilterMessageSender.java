/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 *
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 *
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.daq.filter;

import java.util.Collection;

import javax.annotation.PostConstruct;
import javax.jms.JMSException;

import cern.c2mon.daq.common.conf.core.ProcessConfigurationHolder;
import cern.c2mon.daq.config.DaqProperties;
import lombok.extern.slf4j.Slf4j;

import cern.c2mon.shared.common.filter.FilteredDataTagValue;
import cern.c2mon.shared.common.process.ProcessConfiguration;
import cern.c2mon.shared.daq.filter.FilteredDataTagValueUpdate;
import cern.c2mon.shared.util.buffer.PullEvent;
import cern.c2mon.shared.util.buffer.PullException;
import cern.c2mon.shared.util.buffer.SynchroBuffer;
import cern.c2mon.shared.util.buffer.SynchroBufferListener;


/**
 * Partial implementation of an IFilterMessageSender. Also currently
 * provides the abstract lifecycle methods needed by the DriverKernel.
 * <p>
 * On shutdown, call the closeTagBuffer() method,
 * which will wait for the buffer to empty before returning.
 *
 * @author Mark Brightwell
 */
@Slf4j
public abstract class FilterMessageSender implements IFilterMessageSender {

  /**
   * The SynchroBuffer minimum window size.
   */
  private static final long MIN_WINDOW_SIZE = 200;

  /**
   * The SynchroBuffer window growth factor.
   */
  private static final int WINDOW_GROWTH_FACTOR = 100;

  /**
   * The maximum message delay for the filter module SynchroBuffer.
   */
  private static final long MAX_MESSAGE_DELAY = 1000;

  /**
   * The maximum message size (in terms of number of tag values sent per
   * message)
   */
  private static final int MAX_MESSAGE_SIZE = 100;

  /**
   * The buffer used for collecting the tag update values before sending.
   */
  private SynchroBuffer tagBuffer;

  private DaqProperties properties;

  public FilterMessageSender(DaqProperties properties) {
    this.properties = properties;
  }

  /**
   * Sends the update collection to the filter queue.
   *
   * @param filteredDataTagValueUpdate the update to send
   * @throws JMSException if a problem occurs during sending
   */
  protected abstract void processValues(final FilteredDataTagValueUpdate filteredDataTagValueUpdate) throws JMSException;


  /**
   * Method run at bean initialization.
   */
  @PostConstruct
  public void init() {
    Integer bufferCapacity = properties.getFilter().getBufferCapacity();
    // set up and enable the synchrobuffer for storing the tags
    log.debug("initializing filtering synchrobuffer with max delay :" + MAX_MESSAGE_DELAY + " and capacity : " + bufferCapacity);

    tagBuffer = new SynchroBuffer(MIN_WINDOW_SIZE, MAX_MESSAGE_DELAY, WINDOW_GROWTH_FACTOR, SynchroBuffer.DUPLICATE_OK, bufferCapacity,
        true);
    tagBuffer.setSynchroBufferListener(new SynchroBufferEventsListener());
    tagBuffer.enable();
  }

  /**
   * This method is called from other classes to pass a datatag value for
   * sending to the Filter module. It currently simply adds them to the
   * SynchroBuffer.
   *
   * @param dataTagValue a datatag value to be sent
   */
  @Override
  public void addValue(final FilteredDataTagValue dataTagValue) {
    log.trace("entering addValue()...");
    log.trace("\tadding value to buffer");

    tagBuffer.push(dataTagValue);

    log.trace("...leaving addValue()");
  }

  /**
   * Returns the current size of the buffer used for storing the fitlered values.
   *
   * @return size of the internal buffer
   */
  protected int getBufferSize() {
    return tagBuffer.getSize();
  }

  /**
   * Closes the SynchroBuffer on disconnecting.
   */
  protected void closeTagBuffer() {
    tagBuffer.disable();
    //wait the max time to allow the buffer to empty, then empty it if it fails.
    try {
      Thread.sleep(MAX_MESSAGE_DELAY);
    } catch (InterruptedException e) {
      log.warn("Interrupted exception caught while waiting for filter buffer to empty", e);
    }
    tagBuffer.empty();
    tagBuffer.close();
  }

  /**
   * This class is hooked up with the tagBuffer SynchroBuffer. When a
   * PullEvent is triggered by the SynchroBuffer, the pull method below is
   * called, which then processes all tag values in the buffer.
   *
   * @author mbrightw
   */
  class SynchroBufferEventsListener implements SynchroBufferListener {
    /**
     * When a PullEvent occurs, collects all tag values in the buffer into
     * DataTagValueUpdate objects and forwards them to the processValues
     * method (which then sends them to the JMS broker).
     *
     * @param event the PullEvent triggered by the SynchroBuffer
     * @throws PullException exception in SychroBuffer pull method
     */
    @SuppressWarnings("unchecked")
    @Override
    public void pull(final PullEvent event) throws PullException {
      log.trace("entering FilterMessageSender pull()...");
      log.debug("\t Number of pulled objects : " + event.getPulled().size());
      ProcessConfiguration pconf = ProcessConfigurationHolder.getInstance();
      FilteredDataTagValueUpdate dataTagValueUpdate = new FilteredDataTagValueUpdate(pconf.getProcessID());

      long currentMsgSize = 0;
      for (FilteredDataTagValue filteredTagValue : (Collection<FilteredDataTagValue>) event.getPulled()) {
        // check if the maximum allowed message size has been reached;
        if (currentMsgSize == MAX_MESSAGE_SIZE) {
          // if so, send them (the values have been gathered in the
          // dataTagValueUpdate object)
          try {

            // send the message
            processValues(dataTagValueUpdate);

            // clear the dataTagValueUpdate object reference for the
            // next batch of values
            dataTagValueUpdate = null;

            log.debug("\t sent " + currentMsgSize + " SourceDataTagValue objects to Statistics module");
          } catch (JMSException ex) {
            log.error("\tpull : JMSException caught while invoking processValue method:" + ex.getMessage());
          }

          // create new dataTagValueUpdate object for the next batch
          // of tag values
          dataTagValueUpdate = new FilteredDataTagValueUpdate(pconf.getProcessID());

          // clear the message size counter
          currentMsgSize = 0;

        } // if

        // append next SourceDataTagValue object to the message
        dataTagValueUpdate.addValue(filteredTagValue);

        // increase the message size counter
        currentMsgSize++;

      } // while

      // the final batch of values in dataTagValueUpdate still needs
      // sending, if not empty
      if (dataTagValueUpdate != null && currentMsgSize > 0) {
        try {
          processValues(dataTagValueUpdate);
          log.debug("\t sent " + dataTagValueUpdate.getValues().size() + " FilteredDataTagValue objects");
        } catch (JMSException ex) {
          log.error("\t pull : JMSException caught while invoking processValue method :" + ex.getMessage());
        }
      } // if
      log.trace("leaving FilterMessageSender pull method");
    }
  }
}
