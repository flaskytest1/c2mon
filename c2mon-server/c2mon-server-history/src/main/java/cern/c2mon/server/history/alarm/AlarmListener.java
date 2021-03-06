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
package cern.c2mon.server.history.alarm;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.c2mon.pmanager.IAlarmListener;
import org.springframework.stereotype.Component;

/**
 * Fallback alarm listener that send emails/SMS via log4j.
 *
 * @author Mark Brightwell
 */
@Component
public class AlarmListener implements IAlarmListener {

  /**
   * Mail logger.
   */
  private final static Logger EMAIL_LOGGER = LoggerFactory.getLogger("AdminMailLogger");

  /**
   * SMS logger.
   */
  private final static Logger SMS_LOGGER = LoggerFactory.getLogger("AdminSmsLogger");

  /**
   * Flags for not sending repeated error messages.
   */
  private volatile boolean dbAlarm = false;
  private volatile boolean diskAlarm = false;
  private volatile boolean fileAlarm = false;


  @Override
  public void dbUnavailable(boolean alarmUp, String exceptionMsg, String dbInfo) {
    if (alarmUp && !dbAlarm) {
      dbAlarm = true;
      EMAIL_LOGGER.error("Error in logging to history: DB unavailable with error message " + exceptionMsg + ", for DB " + dbInfo);
      SMS_LOGGER.error("Error in history logging: DB unavailable. See email for details.");
    } else if (!alarmUp && dbAlarm) {
      dbAlarm = false;
      EMAIL_LOGGER.error("DB unavailable error has resolved itself");
      SMS_LOGGER.error("DB unavailable error has resolved itself");
    }
  }

  @Override
  public void diskFull(boolean alarmUp, String directoryName) {
    if (alarmUp && !diskAlarm) {
      diskAlarm = true;
      EMAIL_LOGGER.error("Error in logging to history fallback - the disk is nearly full, directory is " + directoryName);
      SMS_LOGGER.error("Error in history fallback - the disk is nearly full.");
    } else if (!alarmUp && diskAlarm) {
      diskAlarm = false;
      EMAIL_LOGGER.error("Disk full error has resolved itself");
      SMS_LOGGER.error("Disk full error has resolved itself");
    }
  }

  @Override
  public void fileNotReachable(boolean alarmUp, File file) {
    if (alarmUp && !fileAlarm) {
      fileAlarm = true;
      EMAIL_LOGGER.error("Error in logging to history - the following file is not reachable: " + file.getName());
      SMS_LOGGER.error("Error in history fallback - file not reachable: " + file.getName());
    } else if (!alarmUp && fileAlarm) {
      fileAlarm = false;
      EMAIL_LOGGER.error("File unreachable error has resolved itself");
      SMS_LOGGER.error("File unreachable error has resolved itself");
    }
  }

}
