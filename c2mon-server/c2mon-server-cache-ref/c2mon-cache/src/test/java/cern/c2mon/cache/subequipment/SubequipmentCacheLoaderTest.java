package cern.c2mon.cache.subequipment;

import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import cern.c2mon.cache.AbstractCacheLoaderTest;
import cern.c2mon.cache.api.Cache;
import cern.c2mon.server.cache.dbaccess.SubEquipmentMapper;
import cern.c2mon.server.common.subequipment.SubEquipment;
import cern.c2mon.server.common.subequipment.SubEquipmentCacheObject;

import static org.junit.Assert.*;

/**
 * @author Szymon Halastra
 */
public class SubequipmentCacheLoaderTest extends AbstractCacheLoaderTest {

  @Autowired
  private Cache<Long, SubEquipment> subEquipmentCacheRef;

  @Autowired
  private SubEquipmentMapper subEquipmentMapper;

  @Before
  public void init() {
    subEquipmentCacheRef.init();
  }

  @Test
  public void preloadCache() {
    assertNotNull("SubEquipment Cache should not be null", subEquipmentCacheRef);

    List<cern.c2mon.server.common.subequipment.SubEquipment> subEquipmentList = subEquipmentMapper.getAll();

    assertTrue("List of subEquipment tags should not be empty", subEquipmentList.size() > 0);

    assertEquals("Size of cache and DB mapping should be equal", subEquipmentList.size(), subEquipmentCacheRef.getKeys().size());
    //compare all the objects from the cache and buffer
    Iterator<SubEquipment> it = subEquipmentList.iterator();
    while (it.hasNext()) {
      SubEquipmentCacheObject currentSubEquipment = (SubEquipmentCacheObject) it.next();
      //only compares one field so far
      assertEquals("Cached SubEquipment should have the same name as in DB",
              currentSubEquipment.getName(), ((subEquipmentCacheRef.get(currentSubEquipment.getId())).getName()));
    }
  }
}
