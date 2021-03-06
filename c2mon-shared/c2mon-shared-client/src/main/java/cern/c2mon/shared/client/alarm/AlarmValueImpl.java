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
package cern.c2mon.shared.client.alarm;

import java.io.*;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.convert.AnnotationStrategy;
import org.simpleframework.xml.core.Persister;

import cern.c2mon.shared.client.request.ClientRequestReport;

/**
 * This bean class implements the <code>AlarmValue</code> interface and is used
 * to transport an alarm value update information from the server to the client.
 * The <code>AlarmValue</code> is embedded into the
 * <code>TransferTagValue</code> object.
 *
 * @author Matthias Braeger
 *
 * @see cern.c2mon.shared.client.alarm.AlarmValue
 * @see cern.c2mon.shared.client.tag.TagValueUpdate
 */
@Root(name = "AlarmValue")
@Data
@Slf4j
public final class AlarmValueImpl extends ClientRequestReport implements AlarmValue, Cloneable {

  /** Alarm id */
  @NotNull
  @Min(1)
  @Attribute
  private Long id;

  /** LASER alarm fault code */
  @NotNull
  @Element
  private int faultCode;

  /** LASER alarm fault family */
  @NotNull
  @Element
  private String faultFamily;

  // ToDo: correct typo in next major release ->
  // https://gitlab.cern.ch/c2mon/c2mon/issues/149
  /** LASER alarm fault member */
  @NotNull
  @Element
  private String faultMemeber;

  /** Free text for additional information about the alarm */
  @Element(required = false)
  private String info;

  /** Unique identifier of the Tag to which the alarm is attached */
  @NotNull
  @Min(1)
  @Element
  private Long tagId;

  /** Description for the Tag to which the alarm is attached */
  @Element(required = false)
  private String tagDescription;

  /** UTC timestamp of the alarm's last state change */
  @NotNull
  @Past
  @Element
  private Timestamp timestamp;

  /** <code>true</code>, if the alarm is active */
  @Element
  private boolean active;

  /** <code>true</code>, if the oscillation is active */
  @Element
  private boolean oscillating;

  /**
   * Metadata according to the tag in this class.
   */
  @NotNull
  private Map<String, Object> metadata = new HashMap<>();

  /**
   * Hidden Constructor needed for JSON
   */
  private AlarmValueImpl() {
    this(null, -1, null, null, null, null, null, false);
  }

  /**
   * Default Constructor
   * 
   * @param pId
   *          Alarm id
   * @param pFaultCode
   *          LASER alarm fault code
   * @param pFaultMember
   *          LASER alarm fault memeber
   * @param pFaultFamily
   *          LASER alarm fault family
   * @param pInfo
   *          Free text for additional information about the alarm
   * @param pTagId
   *          Unique identifier of the Tag to which the alarm is attached
   * @param pTimestamp
   *          UTC timestamp of the alarm's last state change
   * @param isActive
   *          <code>true</code>, if the alarm is active
   */
  public AlarmValueImpl(final Long pId, final int pFaultCode, final String pFaultMember, final String pFaultFamily, final String pInfo, final Long pTagId,
      final Timestamp pTimestamp, final boolean isActive) {
    id = pId;
    faultCode = pFaultCode;
    faultMemeber = pFaultMember;
    faultFamily = pFaultFamily;
    info = pInfo;
    tagId = pTagId;
    timestamp = pTimestamp;
    active = isActive;
  }

  /**
   * Default Constructor
   * 
   * @param pId
   *          Alarm id
   * @param pFaultCode
   *          LASER alarm fault code
   * @param pFaultMember
   *          LASER alarm fault memeber
   * @param pFaultFamily
   *          LASER alarm fault family
   * @param pInfo
   *          Free text for additional information about the alarm
   * @param pTagId
   *          Unique identifier of the Tag to which the alarm is attached
   * @param pTagDescription
   *          Description for the Tag to which the alarm is attached
   * @param pTimestamp
   *          UTC timestamp of the alarm's last state change
   * @param isActive
   *          <code>true</code>, if the alarm is active
   */
  public AlarmValueImpl(final Long pId, final int pFaultCode, final String pFaultMember, final String pFaultFamily, final String pInfo, final Long pTagId,
      final String pTagDescription, final Timestamp pTimestamp, final boolean isActive) {
    this(pId, pFaultCode, pFaultMember, pFaultFamily, pInfo, pTagId, pTimestamp, isActive);
    tagDescription = pTagDescription;
  }
  
  /**
   * Default Constructor
   * 
   * @param pId
   *          Alarm id
   * @param pFaultCode
   *          LASER alarm fault code
   * @param pFaultMember
   *          LASER alarm fault memeber
   * @param pFaultFamily
   *          LASER alarm fault family
   * @param pInfo
   *          Free text for additional information about the alarm
   * @param pTagId
   *          Unique identifier of the Tag to which the alarm is attached
   * @param pTagDescription
   *          Description for the Tag to which the alarm is attached
   * @param pTimestamp
   *          UTC timestamp of the alarm's last state change
   * @param isActive
   *          <code>true</code>, if the alarm is active
   */
  public AlarmValueImpl(final Long pId, final int pFaultCode, final String pFaultMember, final String pFaultFamily, final String pInfo, final Long pTagId,
      final Timestamp pTimestamp, final boolean isActive, final boolean isOscillating) {
    this(pId, pFaultCode, pFaultMember, pFaultFamily, pInfo, pTagId, pTimestamp, isActive);
    this.oscillating = isOscillating;
  }
  
 

  @Override
  @JsonIgnore
  public String getFaultMember() {
    return this.faultMemeber;
  }

  @Override
  public AlarmValue clone() throws CloneNotSupportedException {
    AlarmValueImpl clone = (AlarmValueImpl) super.clone();
    clone.timestamp = (Timestamp) timestamp.clone();
    clone.metadata = new HashMap<>();
    for (Map.Entry<String, Object> entry : metadata.entrySet()) {
      clone.metadata.put(deepClone(entry.getKey()), deepClone(entry.getValue()));
    }

    return clone;
  }

  private <T> T deepClone(T object) {
    if (object == null) {
      return null;
    }

    try {
      ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
      ObjectOutputStream out = null;
      out = new ObjectOutputStream(byteOut);
      out.writeObject(object);
      out.flush();
      ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(byteOut.toByteArray()));
      return (T) object.getClass().cast(in.readObject());
    } catch (IOException | ClassNotFoundException e) {
      throw new RuntimeException("Error cloning metadata: the object is not serializable");
    }
  }

  public String getXml() {
    Serializer serializer = new Persister(new AnnotationStrategy());
    StringWriter fw = null;
    String result = null;

    try {
      fw = new StringWriter();
      serializer.write(this, fw);
      result = fw.toString();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (fw != null) {
        try {
          fw.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
    return result;
  }

  public static AlarmValueImpl fromXml(final String xml) throws Exception {

    AlarmValueImpl alarmVal = null;
    StringReader sr = null;
    Serializer serializer = new Persister(new AnnotationStrategy());

    try {
      sr = new StringReader(xml);
      alarmVal = serializer.read(AlarmValueImpl.class, new StringReader(xml), false);
    } finally {

      if (sr != null) {
        sr.close();
      }
    }

    return alarmVal;
  }

  @Override
  public String toString() {
    return this.getXml();
  }

  @Override
  public boolean isMoreRecentThan(final AlarmValue alarm) {

    return this.getTimestamp().after(alarm.getTimestamp());
  }
}
