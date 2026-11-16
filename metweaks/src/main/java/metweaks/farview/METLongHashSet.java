package metweaks.farview;

public final class METLongHashSet {

	protected transient METLongHashSet.KeyNode[] hashArray = new METLongHashSet.KeyNode[64];
	private transient int numHashElements;
	private int capacity = 48;
	private int mask = 64 - 1;
	private final float percentUseable = 0.75F;

	private static final int getHashedKey(long key) {
		return (int) key + (int) (key >>> 32) * 92821;
	}

	private static final int getHashIndex(final int hash, final int length) {
		return hash & length - 1;
	}

	public int size() {
		return this.numHashElements;
	}

	public final boolean contains(final long key) {
		for (METLongHashSet.KeyNode entry = this.hashArray[getHashedKey(key) & mask]; entry != null; entry = entry.nextEntry) {
			if (entry.key == key) {
				return true;
			}
		}
		return false;
	}

	public void add(long p_76163_1_) {
		int j = getHashedKey(p_76163_1_);
		int k = getHashIndex(j, this.hashArray.length);

		for (METLongHashSet.KeyNode entry = this.hashArray[k]; entry != null; entry = entry.nextEntry) {
			if (entry.key == p_76163_1_) {
				return;
			}
		}

		this.createKey(j, p_76163_1_, k);
	}

	public void ensureCapacity(int capacity) {
		if (capacity > this.capacity) {
			this.resizeTable(capacity);
		}
	}

	private void resizeTable(int p_76153_1_) {
		METLongHashSet.KeyNode[] aentry = this.hashArray;
		int j = aentry.length;

		if (j == 1073741824) {
			this.capacity = Integer.MAX_VALUE;
		}
		else {
			METLongHashSet.KeyNode[] aentry1 = new METLongHashSet.KeyNode[p_76153_1_];
			this.copyHashTableTo(aentry1);
			this.hashArray = aentry1;
			this.mask = p_76153_1_ - 1;
			this.capacity = (int) ((float) p_76153_1_ * this.percentUseable);
		}
	}

	private void copyHashTableTo(METLongHashSet.KeyNode[] p_76154_1_) {
		METLongHashSet.KeyNode[] aentry = this.hashArray;
		int i = p_76154_1_.length;

		for (int j = 0; j < aentry.length; ++j) {
			METLongHashSet.KeyNode entry = aentry[j];

			if (entry != null) {
				aentry[j] = null;
				METLongHashSet.KeyNode entry1;

				do {
					entry1 = entry.nextEntry;
					int k = getHashIndex(entry.hash, i);
					entry.nextEntry = p_76154_1_[k];
					p_76154_1_[k] = entry;
					entry = entry1;
				}
				while (entry1 != null);
			}
		}
	}

	public long remove(long p_76159_1_) {
		METLongHashSet.KeyNode entry = this.removeKey(p_76159_1_);
		return entry == null ? null : entry.key;
	}

	final METLongHashSet.KeyNode removeKey(long p_76152_1_) {
		int j = getHashedKey(p_76152_1_);
		int k = getHashIndex(j, this.hashArray.length);
		METLongHashSet.KeyNode prev = this.hashArray[k];
		METLongHashSet.KeyNode current;
		METLongHashSet.KeyNode next;

		for (current = prev; current != null; current = next) {
			next = current.nextEntry;

			if (current.key == p_76152_1_) {
				--this.numHashElements;

				if (prev == current) {
					this.hashArray[k] = next;
				}
				else {
					prev.nextEntry = next;
				}

				return current;
			}

			prev = current;
		}

		return current;
	}

	private void createKey(int p_76156_1_, long p_76156_2_, int p_76156_5_) {
		METLongHashSet.KeyNode entry = this.hashArray[p_76156_5_];
		this.hashArray[p_76156_5_] = new METLongHashSet.KeyNode(p_76156_1_, p_76156_2_, entry);

		if (this.numHashElements++ >= this.capacity) {
			this.resizeTable(2 * this.hashArray.length);
		}
	}

	public static final class KeyNode {
		protected final long key;
		protected METLongHashSet.KeyNode nextEntry;
		final int hash;

		KeyNode(int p_i1553_1_, long p_i1553_2_, METLongHashSet.KeyNode p_i1553_5_) {
			this.nextEntry = p_i1553_5_;
			this.key = p_i1553_2_;
			this.hash = p_i1553_1_;
		}

		public final long getKey() {
			return this.key;
		}

		public final boolean equals(Object p_equals_1_) {
			if (!(p_equals_1_ instanceof METLongHashSet.KeyNode)) {
				return false;
			}
			else {
				METLongHashSet.KeyNode entry = (METLongHashSet.KeyNode) p_equals_1_;
				Long olong = Long.valueOf(this.getKey());
				Long olong1 = Long.valueOf(entry.getKey());

				if (olong == olong1 || olong != null && olong.equals(olong1)) {
					return true;
				}

				return false;
			}
		}

		public final int hashCode() {
			return METLongHashSet.getHashedKey(this.key);
		}

		public final String toString() {
			return String.valueOf(this.getKey());
		}
	}
}