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
package cern.c2mon.shared.common.metadata;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

/**
 * Created by fritter on 30/11/15.
 */
@Data
@Slf4j
public class Metadata implements Serializable, Cloneable {

  private Map<String, Object> metadata = new HashMap<>();

  public static String toJSON(Metadata metadata) {
    return toJSON(metadata.getMetadata());
  }

  public static String toJSON(Map<String, Object> metadata) {
    ObjectMapper mapper = new ObjectMapper();
    try {
      return mapper.writeValueAsString(metadata);

    } catch (IOException e) {
      log.error("Exception caught while converting Metadata to a JSON String.", e);
    }
    return null;
  }

  public static Map<String, Object> fromJSON(String jsonString) {
    ObjectMapper mapper = new ObjectMapper();
    try {
      return mapper.readValue(jsonString, Map.class);

    } catch (IOException e) {
      log.error("fromJSON() method unable to create a Map", e);
    }
    return null;
  }

  @Builder
  public Metadata(@Singular("setNewMetadata") Map<String, Object> metadata) {
    this.metadata = metadata;
  }

  public Metadata() {
  }

  public void addMetadata(String key, Object value) {
    metadata.put(key, value);
  }

  public void removeMetadata(String key, Object value) {
    metadata.remove(key, value);
  }

}
