package concurrentHashMaps;
import java.util.Collection;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author SRoy
 * For Copy On Write implementation, the shared data directly read, does not need to be synchronized. 
 * When the data is modified, we put the current data Copy a copy, and then modify.
 * If this copy is complete, then the modified copy replace the original data.
 * @param <K>
 * @param <V>
 */
public class CopyOnWriteBlockingHashMap<K, V> implements Map<K, V>{

	//declare Map variable
	protected volatile Map<K, V> mapToRead = getNewMap ();

	//declare lock
	ReentrantLock tryGetLock = new ReentrantLock();
	ReentrantLock tryGetLockTimed = new ReentrantLock();
	ReentrantLock getInterruptibleLock = new ReentrantLock();

	//method to return hashMap for the declared map variable
	protected Map<K, V> getNewMap () {
		return new HashMap<K, V> ();
	}

	//maintain a copy method
	protected Map<K, V> copy () {
		Map<K, V> newMap = getNewMap ();
		newMap.putAll (mapToRead);
		return newMap;
	}

	//read methods do not need to be locked
	@Override
	public boolean containsKey (Object key) {
		return mapToRead.containsKey (key);
	}

	@Override
	public boolean containsValue (Object value) {
		return mapToRead.containsValue (value);
	}

	@Override
	public boolean isEmpty() {
		return mapToRead.isEmpty ();
	}

	@Override
	public Set<Entry<K, V>> entrySet() {
		return mapToRead.entrySet ();
	}

	@Override
	public int size() {
		return mapToRead.size ();
	}

	@Override
	public Collection<V> values() {
		return mapToRead.values();
	}

	@Override
	public V get(Object key) {
		return mapToRead.get(key);
	}

	@Override
	public Set<K> keySet() {
		return mapToRead.keySet();
	}

	//blocking read methods

	/**
	 * @param key
	 * @return value for the key
	 * this method can make a thread wait forever if the key is never put!
	 */
	public synchronized V tryGet(Object key){		
		V value = null;		
		value = mapToRead.get(key);
		//normal COW read method if value is present. else run loop and wait
		if(value == null){			
			do{
				value = mapToRead.get(key);
				if(value == null)
					try {
							this.wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					finally{
						this.notifyAll();
					//	tryGetLock.unlock();
					}
			}while(value == null); 
		}
		return value;
	}
	/**
	 * @param key
	 * @param waitTime
	 * @return value for the key
	 * wait for specified time
	 */
	public synchronized V tryGet(Object key, int waitTime){		
		V value = null;
		int timeLeft = waitTime;
		long startTime = 0;
		long endTime;
		value = mapToRead.get(key);	
		if(value == null){			
				do{
					value = mapToRead.get(key);
					if(value == null)
						try {
							startTime = System.currentTimeMillis();
								if(timeLeft >= 0){
									System.out.println("key : "+key+"\twaiting...");
									this.wait(timeLeft);
								}
		
							else{
								return null;
							}
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						finally{
								endTime = System.currentTimeMillis();
								timeLeft = (int) (waitTime - (endTime - startTime));
								this.notifyAll();
						}
				}while(value == null); 
		}			
		return value;
	}	

	/**
	 * @param key
	 * @return
	 */
	public synchronized V getInterruptably(Object key){		
		V value = null;		
		value = mapToRead.get(key);
		//normal COW read method if value is present. else run loop and wait
		if(value == null){			
			//interruptible lock
			do{
				value = mapToRead.get(key);
				if(value == null)
					try {
						getInterruptibleLock.lockInterruptibly();
						this.wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					finally{
						this.notifyAll();
						getInterruptibleLock.unlock();
					}
			}while(value == null); 
		}
		return value;
	}

	//write methods need to be locked
	@Override
	public synchronized void clear () {
		mapToRead = getNewMap ();
	}

	@Override
	public synchronized V put (K key, V value) {
		Map<K, V> map = copy ();
		V val = map.put (key, value);
		mapToRead = map;
		this.notifyAll();
		return val;
	}

	@Override
	public synchronized void putAll (Map<? extends K, ? extends V> t) {
		Map<K, V> map = copy ();
		map.putAll (t);
		mapToRead = map;
		this.notifyAll();
	}

	@Override
	public synchronized V remove (Object key) {
		Map<K, V> map = copy ();
		V objRem = map.remove (key);
		mapToRead = map;
		return objRem;
	}
}
