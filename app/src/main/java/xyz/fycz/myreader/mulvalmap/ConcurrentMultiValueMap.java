package xyz.fycz.myreader.mulvalmap;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author fengyue
 * @date 2020/5/19 7:36
 */

public class ConcurrentMultiValueMap<K, V> implements MultiValueSetMap<K, V> {

    protected Map<K, HashSet<V>> mSource = new ConcurrentHashMap<>();

    public ConcurrentMultiValueMap() {
    }

    @Override
    public void add(K key, V value) {
        if (key != null) {
            // 如果有这个Key就继续添加Value，没有就创建一个List并添加Value
            if (!mSource.containsKey(key))
                mSource.put(key, new HashSet<V>(2));
            mSource.get(key).add(value);
        }
    }

    @Override
    public void add(K key, HashSet<V> values) {
        // 便利添加进来的List的Value，调用上面的add(K, V)方法添加
        for (V value : values) {
            add(key, value);
        }
    }

    @Override
    public void addAll(MultiValueSetMap<K, V> mvm) {
        for(K k : mvm.keySet()){
            add(k, new HashSet<V>(mvm.getValues(k)));
        }
    }

    @Override
    public void set(K key, V value) {
        // 移除这个Key，添加新的Key-Value
        mSource.remove(key);
        add(key, value);
    }

    @Override
    public void set(K key, HashSet<V> values) {
        // 移除Key，添加List<V>
        mSource.remove(key);
        add(key, values);
    }

    @Override
    public void set(Map<K, HashSet<V>> map) {
        // 移除所有值，便利Map里的所有值添加进来
        mSource.clear();
        mSource.putAll(map);
    }

    @Override
    public HashSet<V> remove(K key) {
        return mSource.remove(key);
    }

    @Override
    public void clear() {
        mSource.clear();
    }

    @Override
    public Set<K> keySet() {
        return mSource.keySet();
    }

    @Override
    public List<V> values() {
        // 创建一个临时List保存所有的Value
        List<V> allValues = new ArrayList<V>();

        // 便利所有的Key的Value添加到临时List
        Set<K> keySet = mSource.keySet();
        for (K key : keySet) {
            allValues.addAll(mSource.get(key));
        }
        return allValues;
    }

    @Override
    public List<V> getValues(K key) {
        return (mSource.get(key) != null) ? new ArrayList<>(mSource.get(key)) : null;
    }

    @Override
    public V getValue(K key, int index) {
        List<V> values = getValues(key);
        if (values.size() > 0 && index < values.size())
            return values.get(index);
        return null;
    }

    @Override
    public int size() {
        return mSource.size();
    }

    @Override
    public boolean isEmpty() {
        return mSource.isEmpty();
    }

    @Override
    public boolean containsKey(K key) {
        return mSource.containsKey(key);
    }

}

