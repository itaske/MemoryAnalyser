//TO DO: Complete this class, add JavaDocs

//Do not add any more imports!
import java.util.Iterator;
import java.util.Set;

public class MemMan implements Iterable<MemBlock> {

	//******************************************************
	//****    IMPORTANT: DO NOT CHANGE/ALTER/REMOVE     ****
	//****    ANYTHING PROVIDED LIKE THESE INTS HERE... ****
	//******************************************************
	
	public static final int FIRST_FIT = 0;
	public static final int BEST_FIT = 1;
	public static final int WORST_FIT = 2;
	public static final int NEXT_FIT = 3;


	//******************************************************
	//****    IMPORTANT: DO NOT CHANGE/ALTER/REMOVE     ****
	//****    ANYTHING PROVIDED LIKE THE INSTANCE       ****
	//****    VARIABLES IN THIS NESTED CLASS. ALSO      ****
	//****    DON'T REMOVE THE CLASS ITSELF OR ANYTHING ****
	//****    STRANGE LIKE THAT.                        ****
	//******************************************************
	public static class BareNode {
		public MemBlock block;
		public BareNode next;
		public BareNode prev;
		public boolean marked;
		
		public BareNode(MemBlock block) {
			this.block = block;
		}

		@Override
		public String toString() {
			return "BareNode{" +
					"block=" + block +
					", next=" + (next == null ? null:next.block) +
					", prev=" + (prev == null ? null:prev.block) +
					", marked=" + marked +
					'}';
		}
	}
	
	public BareNode head;
	public int currentType;
	public BareNode nextFitNodePointer;

	protected MemMan(int type, BareNode head){
		this.head = head;
		this.currentType = type;
		this.nextFitNodePointer = head;
	}
	
	
	
	public static MemMan factory(int type, BareNode head) {
		if (type >= 0 && type < 4 && head != null){
			return new MemMan(type, head);
		}
		return null;
	}
	
	public BareNode getHead() {
		return head;
		
	}
	
	public BareNode malloc(int size) {
		switch (currentType){
			case FIRST_FIT: return getFirstFitMalloc(head, size);
			case BEST_FIT: return getBestFitMalloc(size);
			case WORST_FIT: return getWorseFitMalloc(size);
			case NEXT_FIT: return getFirstFitMalloc(nextFitNodePointer, size);
			default: return null;
		}
	}


	private BareNode getWorseFitMalloc(int size) {
		BareNode bestNode = null;
		BareNode currentNode = head;
		while(currentNode != null){
			if (currentNode.block.isFree && currentNode.block.size >= size){
				if (bestNode==null || currentNode.block.size > bestNode.block.size)
					bestNode = currentNode;
			}
			currentNode = currentNode.next;
		}


		if (bestNode != null)
			bestNode = manipulateBlock(bestNode, size);

		nextFitNodePointer = bestNode;
		return bestNode;
	}

	private BareNode getBestFitMalloc(int size) {
		BareNode bestNode = null;
        BareNode currentNode = head;
		while(currentNode != null){
			if (currentNode.block.isFree && currentNode.block.size >= size){
				if (bestNode==null || currentNode.block.size < bestNode.block.size)
					bestNode = currentNode;
			}
			currentNode = currentNode.next;
		}


		if (bestNode != null)
			bestNode = manipulateBlock(bestNode, size);

		nextFitNodePointer = bestNode;
		return bestNode;
	}

	private BareNode getFirstFitMalloc(BareNode startNode, int size) {
		BareNode currentNode = startNode;
		while(currentNode != null){
			if (currentNode.block.isFree && currentNode.block.size >= size){
				currentNode = manipulateBlock(currentNode, size);
				break;
			}
			currentNode = currentNode.next;
		}

		nextFitNodePointer = currentNode;
		if (nextFitNodePointer == null)
			nextFitNodePointer = head;

		return currentNode;
	}

	private BareNode manipulateBlock(BareNode currentNode, int size) {
		if (currentNode.block.size > size){
			currentNode = splitBlock(currentNode, size);

		}else{
			MemBlock memBlock = new MemBlock(currentNode.block.addr, currentNode.block.size, false);
			BareNode memNode = new BareNode(memBlock);

			memNode.next = currentNode.next;
			memNode.prev = currentNode.prev;

			currentNode.prev.next = memNode;

			currentNode = memNode;
		}
		return currentNode;
	}

	public boolean free(BareNode node) {
		if (node == null || node.block.isFree)
			return false;
		MemBlock memBlock = new MemBlock(node.block.addr, node.block.size, true);
		node.block = memBlock;
		return true;
	}
	
	public BareNode realloc(BareNode node, int size) {
		MemBlock memBlock = new MemBlock(node.block.addr, size, false);
		BareNode memNode = new BareNode(memBlock);
		if (size > node.block.size){
			if (node.next != null && node.next.block.isFree && node.next.block.size + node.block.size >size){
				MemBlock freeBlock = new MemBlock(node.block.addr +  node.block.size, node.next.block.size+ node.block.size-size, true);
				BareNode freeNode = new BareNode(freeBlock);

				memNode.next = freeNode;
				freeNode.prev = memNode;
				memNode.prev = node.prev;

				if (node.prev != null)
					node.prev.next = memNode;
				else {
					head = node;
				}


				if (node.next != null) {
					node.next.prev = freeNode;
					freeNode.next = node.next;
				}

				node = memNode;

			} else {
				free(node);
				return malloc(size);
			}
		}else if (size < node.block.size){
			MemBlock freeBlock;
			BareNode freeNode;
			if (node.next != null && node.next.block.isFree){
				freeBlock = new MemBlock(node.block.addr + size, node.next.block.size + (node.block.size - size), true);
			} else{
				freeBlock = new MemBlock(node.block.addr + size, node.block.size - size, true);
			}
			freeNode = new BareNode(freeBlock);
			memNode.next = freeNode;
			freeNode.prev = memNode;
			memNode.prev = node.prev;


			if (node.prev != null)
				node.prev.next = memNode;
			else {
				head = memNode;
			}

			if (node.next!=null) {
				node.next.prev = freeNode;
				freeNode.next = node.next;
			}

			node = memNode;

		}

		return memNode;
	}
	
	public int garbageCollect(Set<Integer> addrs) {
		BareNode currentNode = head;
        int bytes = 0;
		if (!addrs.isEmpty()){
			while (currentNode!=null){
				if (addrs.contains(currentNode.block.addr) && !currentNode.block.isFree){
					currentNode.marked = true;
				}
				currentNode = currentNode.next;
			}
			currentNode = head;
			while (currentNode!=null){
				if (!currentNode.marked && !currentNode.block.isFree){
					free(currentNode);
					bytes+=currentNode.block.size;
				} else{
					currentNode.marked = false;
				}
				currentNode = currentNode.next;
			}
		}
		return bytes;
	}
	

	public Iterator<MemBlock> iterator(){
		return new MemManIterator(head);
	}

	public class MemManIterator implements Iterator<MemBlock>{

		public BareNode currentNode;
		public MemManIterator(BareNode head){
			this.currentNode = head;
		}

		@Override
		public boolean hasNext() {
			return currentNode != null;
		}

		@Override
		public MemBlock next() {
			MemBlock oldNode = currentNode.block;
			currentNode = currentNode.next;
			return oldNode;
		}
	}

	private BareNode splitBlock(BareNode currentNode, int size){
		MemBlock freeBlock = new MemBlock(currentNode.block.addr+size, currentNode.block.size - size, true);
		BareNode freeNode = new BareNode(freeBlock);

		MemBlock memBlock = new MemBlock(currentNode.block.addr, size, false);
		BareNode memNode = new BareNode(memBlock);

		memNode.next = freeNode;
		freeNode.prev = memNode;
		memNode.prev = currentNode.prev;


		if (currentNode.prev != null)
			currentNode.prev.next = memNode;
		else {
			head = memNode;
		}

		if (currentNode.next!=null) {
			currentNode.next.prev = freeNode;
			freeNode.next = currentNode.next;
		}

		currentNode = memNode;

       return currentNode;
	}
	
}

 


