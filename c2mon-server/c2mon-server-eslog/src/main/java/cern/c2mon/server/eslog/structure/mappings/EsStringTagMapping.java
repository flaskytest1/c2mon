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
package cern.c2mon.server.eslog.structure.mappings;

import cern.c2mon.server.eslog.structure.types.tag.EsTagString;
import cern.c2mon.server.eslog.structure.types.tag.EsValueType;

/**
 * EsMapping that a {@link EsTagString} will use to be indexed in the ElasticSearch cluster.
 * valueString = value; valueBoolean = value = null;
 *
 * @author Alban Marguet.
 */
public class EsStringTagMapping extends EsTagMapping implements EsMapping {
  /**
   * Instantiate a new EsStringTagMapping by setting its Properties according to {@param the type}.
   */
  public EsStringTagMapping() {
    super(EsValueType.STRING);
  }
}