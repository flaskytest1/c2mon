/*******************************************************************************
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
 ******************************************************************************/

package cern.c2mon.server.elasticsearch.structure.types.tag;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import cern.c2mon.pmanager.IFallback;
import cern.c2mon.pmanager.fallback.exception.DataFallbackException;
import cern.c2mon.server.elasticsearch.structure.types.GsonSupplier;

/**
 * @author Szymon Halastra
 */
@Data
@Slf4j
public class EsTagConfig implements IFallback {

  @NonNull
  protected static final transient Gson gson = GsonSupplier.INSTANCE.get();

  protected long id;
  protected String name;
  protected final Map<String, String> metadata = new HashMap<>();

  private final EsTagC2monInfo c2mon;

  public EsTagConfig() {
    this.id = -1L;
    this.c2mon = new EsTagC2monInfo("String");
  }

  public EsTagConfig(Long id, String dataType) {
    this.id = id;
    this.c2mon = new EsTagC2monInfo(dataType);
  }

  @Override
  public IFallback getObject(String line) throws DataFallbackException {
    return gson.fromJson(line, EsTagConfig.class);
  }

  @Override
  public String getId() {
    return String.valueOf(id);
  }

  @Override
  public String toString() {
    String json = gson.toJson(this);
    log.debug(json);
    return json;
  }
}
