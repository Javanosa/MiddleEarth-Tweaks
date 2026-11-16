package metweaks.farview;

import net.minecraft.util.LongHashMap;

public class METLongHashMap extends LongHashMap {

	public transient METLongHashMap.Bucket[] hashArray = new METLongHashMap.Bucket[64];
	private transient int numHashElements;
	private int capacity = 48;
	private final float percentUseable = 0.75F;

	private static int getHashedKey(long key) {
		return (int) key + (int) (key >>> 32) * 92821;
	}

	private static int getHashIndex(int hash, int length) {
		return hash & length - 1;
	}

	@Override
	public int getNumHashElements() {
		return this.numHashElements;
	}

	@Override
	public Object getValueByKey(long key) {
		int hash = getHashedKey(key);

		for (METLongHashMap.Bucket entry = this.hashArray[getHashIndex(hash, this.hashArray.length)]; entry != null; entry = entry.nextEntry) {
			if (entry.key == key) {
				return entry.value;
			}
		}

		return null;
	}

	@Override
	public boolean containsItem(long key) {
		return this.getEntryM(key) != null;
	}

	final METLongHashMap.Bucket getEntryM(long key) {
		int hash = getHashedKey(key);

		for (METLongHashMap.Bucket entry = this.hashArray[getHashIndex(hash, this.hashArray.length)]; entry != null; entry = entry.nextEntry) {
			if (entry.key == key) {
				return entry;
			}
		}

		return null;
	}

	@Override
	public void add(long key, Object value) {
		int hash = getHashedKey(key);
		int i = getHashIndex(hash, this.hashArray.length);

		for (METLongHashMap.Bucket entry = this.hashArray[i]; entry != null; entry = entry.nextEntry) {
			if (entry.key == key) {
				entry.value = value;
				return;
			}
		}

		this.createKey(hash, key, value, i);
	}

	private void resizeTable(int capacity) {
		METLongHashMap.Bucket[] entries = this.hashArray;
		int oldcapacity = entries.length;

		if (oldcapacity == 1073741824) {
			this.capacity = Integer.MAX_VALUE;
		}
		else {
			METLongHashMap.Bucket[] newEntries = new METLongHashMap.Bucket[capacity];
			this.copyHashTableTo(newEntries);
			this.hashArray = newEntries;
			this.capacity = (int) ((float) capacity * this.percentUseable);
		}
	}

	private void copyHashTableTo(METLongHashMap.Bucket[] entries) {
		METLongHashMap.Bucket[] from = this.hashArray;
		int i = entries.length;

		for (int j = 0; j < from.length; ++j) {
			METLongHashMap.Bucket entry = from[j];

			if (entry != null) {
				from[j] = null;
				METLongHashMap.Bucket entry1;

				do {
					entry1 = entry.nextEntry;
					int k = getHashIndex(entry.hash, i);
					entry.nextEntry = entries[k];
					entries[k] = entry;
					entry = entry1;
				}
				while (entry1 != null);
			}
		}
	}

	@Override
	public Object remove(long key) {
		METLongHashMap.Bucket entry = this.removeKeyM(key);
		return entry == null ? null : entry.value;
	}


	final METLongHashMap.Bucket removeKeyM(long key) {
		int j = getHashedKey(key);
		int k = getHashIndex(j, this.hashArray.length);
		METLongHashMap.Bucket entry = this.hashArray[k];
		METLongHashMap.Bucket entry1;
		METLongHashMap.Bucket entry2;

		for (entry1 = entry; entry1 != null; entry1 = entry2) {
			entry2 = entry1.nextEntry;

			if (entry1.key == key) {
				--this.numHashElements;

				if (entry == entry1) {
					this.hashArray[k] = entry2;
				}
				else {
					entry.nextEntry = entry2;
				}

				return entry1;
			}

			entry = entry1;
		}

		return entry1;
	}

	private void createKey(int p_76156_1_, long p_76156_2_, Object p_76156_4_, int p_76156_5_) {
		METLongHashMap.Bucket entry = this.hashArray[p_76156_5_];
		this.hashArray[p_76156_5_] = new METLongHashMap.Bucket(p_76156_1_, p_76156_2_, p_76156_4_, entry);

		if (this.numHashElements++ >= this.capacity) {
			this.resizeTable(2 * this.hashArray.length);
		}
	}

	public static class Bucket {
		final long key;
		Object value;
		public METLongHashMap.Bucket nextEntry;
		final int hash;


		Bucket(int p_i1553_1_, long p_i1553_2_, Object p_i1553_4_, METLongHashMap.Bucket p_i1553_5_) {
			this.value = p_i1553_4_;
			this.nextEntry = p_i1553_5_;
			this.key = p_i1553_2_;
			this.hash = p_i1553_1_;
		}

		public final long getKey() {
			return this.key;
		}

		public final Object getValue() {
			return this.value;
		}

		public final boolean equals(Object compare) {
			if (!(compare instanceof METLongHashMap.Bucket)) {
				return false;
			}
			else {
				METLongHashMap.Bucket entry = (METLongHashMap.Bucket) compare;
				Long olong = Long.valueOf(this.getKey());
				Long olong1 = Long.valueOf(entry.getKey());

				if (olong == olong1 || olong != null && olong.equals(olong1)) {
					Object object1 = this.getValue();
					Object object2 = entry.getValue();

					if (object1 == object2 || object1 != null && object1.equals(object2)) {
						return true;
					}
				}

				return false;
			}
		}

		public final int hashCode() {
			return METLongHashMap.getHashedKey(this.key);
		}

		public final String toString() {
			return this.getKey() + "=" + this.getValue();
		}
	}
}