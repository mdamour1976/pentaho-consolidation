package org.pentaho.di.job.entry.validator;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ValidatorContext {

  private Map<String, Object> map = new HashMap<String, Object>();

  public Map<String, Object> getMap() {
    return map;
  }

  public ValidatorContext put(String key, Object value) {
    map.put(key, value);
    return this;
  }

  public ValidatorContext putAsList(String key, Object... value) {
    map.put(key, value);
    return this;
  }

  public void clear() {
    map.clear();
  }

  public boolean containsKey(String key) {
    return map.containsKey(key);
  }

  public boolean containsValue(Object value) {
    return map.containsValue(value);
  }

  public Set<Map.Entry<String, Object>> entrySet() {
    return map.entrySet();
  }

  public Object get(String key) {
    return map.get(key);
  }

  public boolean isEmpty() {
    return map.isEmpty();
  }

  public Set<String> keySet() {
    return map.keySet();
  }

  public ValidatorContext putAll(Map t) {
    map.putAll((Map<String, Object>) t);
    return this;
  }

  public Object remove(String key) {
    return map.remove(key);
  }

  public int size() {
    return map.size();
  }

  public Collection values() {
    return map.values();
  }

}
