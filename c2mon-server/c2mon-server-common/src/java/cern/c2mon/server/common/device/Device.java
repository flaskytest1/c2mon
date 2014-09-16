/*******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 *
 * Copyright (C) 2004 - 2014 CERN. This program is free software; you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received
 * a copy of the GNU General Public License along with this program; if not,
 * write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 *
 * Author: TIM team, tim.support@cern.ch
 ******************************************************************************/
package cern.c2mon.server.common.device;

import java.util.List;

import cern.c2mon.shared.client.device.DeviceCommand;
import cern.c2mon.shared.client.device.DeviceProperty;
import cern.c2mon.shared.common.Cacheable;

/**
 * This interface describes the methods provided by a Device object used in the
 * server Device cache.
 *
 * @author Justin Lewis Salmon
 */
public interface Device extends Cacheable {

  /**
   * Retrieve the unique ID of this device.
   *
   * @return the device ID
   */
  @Override
  public Long getId();

  /**
   * Retrieve the name of this device.
   *
   * @return the device name
   */
  public String getName();

  /**
   * Retrieve the ID of the class to which this device belongs.
   *
   * @return the device class ID
   */
  public Long getDeviceClassId();

  /**
   * Retrieve the device properties of this device.
   *
   * @return the list of device properties
   */
  public List<DeviceProperty> getDeviceProperties();

  /**
   * Retrieve the device commands of this device.
   *
   * @return the list of device commands
   */
  public List<DeviceCommand> getDeviceCommands();

}
