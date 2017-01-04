package mdp;
import java.util.Iterator;
import java.util.LinkedList;


public class ImmRewardList extends LinkedList{
	private ImmRewardList list;
	
	public ImmRewardList(){
		super();
	}
	
	public void putElement(int[] element){
		list.add(element);
        }
       
    @Override
	public boolean isEmpty(){
           return list.isEmpty();
        }
        public void removeList(){
            Iterator it = list.iterator();
            list.removeAll(list);
        }
	public int[] getElement(){
		Iterator it = list.iterator();
		int[] intArr = (int[])it.next();
                return intArr;
	}
	
}
