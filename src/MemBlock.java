//TO DO: Add JavaDocs, you can add more things too, but don't break anything

public class MemBlock {
	//******************************************************
	//****    IMPORTANT: DO NOT CHANGE/ALTER/REMOVE     ****
	//****    ANYTHING PROVIDED LIKE THESE INTS, THIS   ****
	//****    BOOLEAN, OR THE CONSTRUCTOR.              ****
	//******************************************************
	
	public final int addr;
	public final int size;
	public final boolean isFree;
		
	public MemBlock(int addr, int size, boolean isFree) {
		this.addr = addr;
		this.size = size;
		this.isFree = isFree;
	}
	
	//some "copy constructors" might be useful here...


	@Override
	public String toString() {
		return "MemBlock{" +
				"addr=" + addr +
				", size=" + size +
				", isFree=" + isFree +
				'}';
	}
}