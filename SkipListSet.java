//SkipListSet by Scott McLeod

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.Random;

public class SkipListSet<T extends Comparable<T>> implements SortedSet<T>
{
	private int height; //set height
	private int size; //number of elements in set
	private SkipListSetItem head; //head node, initialized to null
	private SkipListSetItem tail; //tail node, initialized to null
	
	public class SkipListSetIterator<T extends Comparable<T>> implements Iterator<T>
	{
		public SkipListSetItem current; //node the iterator is currently using
		@Override
		public boolean hasNext() 
		{
			if (current != tail) //tail is the last node, so if we're not at tail, we must have another node
			{
				return true;
			}
			return false;
		}

		@Override
		public T next() 
		{
			if (current != tail) //tail is the last node, so if we're not at tail, we must have another node
			{
				current = current.nextNodes.get(0);
				return (T)current.value;
			}
			return null; //should only ever trigger if current node is tail
		}
		
		public void remove()
		{
			SkipListSet.this.remove(current.value); //gets rid of current node
		}
		
		public SkipListSetIterator()
		{
			current = head; //sets current to head initially
		}
	}
	
	private class SkipListSetItem
	{
		ArrayList<SkipListSetItem> nextNodes; //arraylist of nodes to right
		ArrayList<SkipListSetItem> prevNodes; //arraylist of nodes to left
		int nodeHeight;
		public T value; //value of node
		public SkipListSetItem(T val)
		{
			nodeHeight = 0;
			value = val;
			
			Random rand = new Random();
			while (true) //this ultimately sets height as described in assignment instructions
			{
				boolean randBool = rand.nextBoolean(); //coin flip
				nodeHeight++;
				if (randBool) //if heads or tails basically
				{
					break;
				}
			}
			if (nodeHeight > height) //override height if node's height is greater than allowed height
			{
				nodeHeight = height;
			}
			nextNodes = new ArrayList<SkipListSetItem>();
			prevNodes = new ArrayList<SkipListSetItem>();
		}
	}

	public SkipListSet()
	{
		height = 3;
		size = 2; //head and tail
		head = new SkipListSetItem(null);
		tail = new SkipListSetItem(null);
		head.nodeHeight = height;
		tail.nodeHeight = height;
		for (int i = 0; i < 3; i++) //initializes pointers of head and tail to each other
		{
			head.nextNodes.add(tail);
			tail.prevNodes.add(head);
		}
	}

	public SkipListSet(Collection<T> collec) //works
	{
		height = 3;
		size = 2; //head and tail
		head = new SkipListSetItem(null);
		tail = new SkipListSetItem(null);
		head.nodeHeight = height;
		tail.nodeHeight = height;
		
		for (int i = 0; i < 3; i++) //initializes pointers of head and tail to each other
		{
			head.nextNodes.add(tail);
			tail.prevNodes.add(head);
		}
		addAll(collec); //adds collection in argument of constructor
	}

	public boolean add(T element) 
	{
		if (contains(element)) //checks if node with same value already is in set; if so, does not add
		{
			return false;
		}
		SkipListSetItem newNode = new SkipListSetItem(element); //to be put into list
		SkipListSetItem temp = head; //used for moving through list
		int tempHeight = height - 1;
		
		size++;
		while (true)
		{
			if (temp.nextNodes.get(tempHeight).value == null)//essentially if next node is tail
			{
				if (tempHeight == 0)
				{
					for (int i = 0; i < newNode.nodeHeight; i++) //for each entry in arraylist, change pointers
					{
						newNode.prevNodes.add(tail.prevNodes.get(i));
						
						newNode.nextNodes.add(tail);
						
						tail.prevNodes.get(i).nextNodes.set(i, newNode);
						
						tail.prevNodes.set(i, newNode);
					}
					break;
				}
				tempHeight--;
				continue;
			}
			else if ((temp.nextNodes.get(tempHeight).value.compareTo(newNode.value) < 0) && (temp.nextNodes.get(tempHeight) != tail))//current nodes are still too small
			{
				temp = temp.nextNodes.get(tempHeight); //move to right node
				continue;
			}
			else if ((temp.nextNodes.get(tempHeight).value.compareTo(newNode.value) > 0) || (temp.nextNodes.get(tempHeight) == tail))//found place to insert
			{
				if (tempHeight == 0) //can't go down anymore
				{
					for (int i = 0; i < newNode.nodeHeight; i++) //for each entry in new node's arraylist
					{
						for (int j = 0; j < size; j++) //goes through potentially every other node
						{
							if (temp.nodeHeight > i) //if temp node's height is high enough to be linked, link it
							{
								SkipListSetItem tempNext = temp.nextNodes.get(i);
								
								newNode.prevNodes.add(temp);
								
								newNode.nextNodes.add(tempNext);
								
								if (i <= temp.nodeHeight) //prevents out of bounds errors
								{
									temp.nextNodes.set(i, newNode);
								}
								
								if (i <= tempNext.nodeHeight)
								{
									tempNext.prevNodes.set(i, newNode);
								}
								
								break;
							}
							else //keep looking for nodes to set the next pointer to
							{
								temp = temp.prevNodes.get(0);
							}
						}
					}
					break;
				}
				tempHeight--;
			}
			
		}
		if (size >= 8) //prevents extreme changes of height in small lists
		{
			int tempSize = size;
			while (tempSize >= 2)
			{
				if (tempSize % 2 == 0 && tempSize >= 2) //ultimately sees if size is a power of 2
				{
					tempSize /= 2;
					if (tempSize == 1)
					{
						height++; //increase overall height of list
						head.nodeHeight++;
						tail.nodeHeight++;
						head.nextNodes.add(tail);
						tail.prevNodes.add(head);
						break;
					}
				}
				else
				{
					break;
				}
			}
		}
		
		return true;
	}
	
	@Override
	public boolean addAll(Collection<? extends T> collec)
	{
		boolean addFlag = false;
		boolean tempFlag = false;
		for (T i : collec)//for each element in the collection
		{
			tempFlag = add(i);//add the element
			if (tempFlag)
			{
				addFlag = true;//if any are added, will ultimately return true
			}
		}
		return addFlag;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public boolean contains(Object o)
	{
		T tempElement = (T)o; //to be compared to in order to find node
		SkipListSetItem temp = head; //used to traverse through list
		int tempHeight = temp.nextNodes.size() - 1;
		
		while (temp != tail)
		{
			if (tempHeight >= temp.nextNodes.size())//prevents an index out of bounds error
			{
				tempHeight--;
				continue;
			}
			if (temp.nextNodes.get(tempHeight).value == null)//if next is tail, essentially
			{
				if (tempHeight == 0)//can't go down any further, we did not find the node
				{
					return false;
				}
				tempHeight--;
				continue;
			}
			else if ((temp.nextNodes.get(tempHeight).value.compareTo(tempElement) < 0) && (temp.nextNodes.get(tempHeight) != tail))//need to keep going
			{
				temp = temp.nextNodes.get(tempHeight);
				continue;
			}
			else if ((temp.nextNodes.get(tempHeight).value.compareTo(tempElement) > 0) || (temp.nextNodes.get(tempHeight) == tail))//gone too far
			{
				if (tempHeight == 0)//can't go down any further, we did not find the node
				{
					return false;
				}
				tempHeight--;
			}
			else if (temp.nextNodes.get(tempHeight).value.compareTo(tempElement) == 0)//found the element, return true
			{
				return true;
			}
		}
		return false;
	}	

	@Override
	public void clear()
	{
		height = 3;//head and tail
		size = 2;
		head.nextNodes.clear();//clear head's next nodes
		tail.prevNodes.clear();//clear tail's prev nodes, doing both brings garbage collection to dispose of other nodes
		
		head.nodeHeight = height;//reset height
		tail.nodeHeight = height;//reset height
		for (int i = 0; i < 3; i++)//same as when skiplistset is initially made
		{
			head.nextNodes.add(tail);
			tail.prevNodes.add(head);
		}
		
		return;
	}

	@Override
	public boolean containsAll(Collection<?> collec)
	{
		boolean containsFlag = true;
		boolean tempFlag = true;
		for (Object i : collec)//goes through entire collection
		{
			tempFlag = contains(i);//checks to see if each element is contained
			if (!tempFlag)
			{
				containsFlag = false;//if any are missing, sets flag to false
			}
		}
		return containsFlag;
	}
	
	public boolean equals(Object o)
	{
		if (o.hashCode() != hashCode()) //if the hashcodes aren't equal, return false. if they are, return true.
		{
			return false;
		}
		return true;
	}

	@Override
	public boolean isEmpty()
	{
		if (size == 2) //head and tail
		{
			return true;
		}
		return false;
	}
	
	public int hashCode()
	{
		SkipListSetItem temp = head;
		int sum = 0;
		for (int i = 0; i < size - 2; i++) //avoids head and tail
		{
			temp = temp.nextNodes.get(0);
			sum += temp.value.hashCode(); //add sum of all hashcodes of all elements together to make list's hashcode
		}
		return sum;
	}

	@Override
	public Iterator<T> iterator() 
	{
		SkipListSet<T>.SkipListSetIterator<T> iter = new SkipListSetIterator<T>(); //new iterator
		return (Iterator<T>)iter;
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean remove(Object o)
	{
		T tempElement = (T)o; //element to remove if found
		SkipListSetItem temp = head; //traverses list
		int tempHeight = height - 1;
		
		while (true) //will eventually be broken
		{
			if (temp.nextNodes.get(tempHeight).value == null)//checks if next node is null (aka tail)
			{
				if (tempHeight == 0) //can't go any further down
				{
					return false; //couldn't find
				}
				tempHeight--;
			}
			else if (temp.nextNodes.get(tempHeight).value.compareTo(tempElement) == 0)//if temp and current node are equal, remove
			{
				SkipListSetItem rmvNode = temp.nextNodes.get(tempHeight); //node to be removed
				for (int i = 0; i < rmvNode.nodeHeight; i++)
				{
					if (rmvNode != head && rmvNode != tail) //prevents null pointers
					{
						rmvNode.nextNodes.get(i).prevNodes.set(i, rmvNode.prevNodes.get(i));
						rmvNode.prevNodes.get(i).nextNodes.set(i, rmvNode.nextNodes.get(i));
					}		
				}
				size--;
				int tempSize = size;
				if (size >= 8) //prevents extreme height manipulation early on in list formation
				{
					while (tempSize >= 2)
					{
						if (tempSize % 2 == 0 && tempSize >= 2) //will ultimately find if size is a power of 2
						{
							tempSize /= 2;
							if (tempSize == 1)
							{
								height--; //reduce height
								head.nextNodes.remove(head.nodeHeight - 1);
								tail.prevNodes.remove(tail.nodeHeight - 1);
								head.nodeHeight--;
								tail.nodeHeight--;
								SkipListSetItem heightReducer = head.nextNodes.get(height - 1);
								while (true)
								{
									if (heightReducer.nodeHeight > height) //if a node's height is greater than the new height
									{
										heightReducer.nodeHeight--; //make it the same height and remove nodes
										heightReducer.nextNodes.remove(heightReducer.nodeHeight);
										heightReducer.prevNodes.remove(heightReducer.nodeHeight);
									}
									if (heightReducer == tail)
									{
										break; //stop if at tail
									}
									heightReducer = heightReducer.nextNodes.get(height - 1); //only need to traverse the top height
								}
								break;
							}
						}
						else
						{
							break;
						}
					}
				}
				return true; //found it
			}
			else if ((temp.nextNodes.get(tempHeight).value.compareTo(tempElement) > 0)) //if your curr value is greater than target
			{
				if (tempHeight == 0) //can't go down any further
				{
					return false;
				}
				tempHeight--; //go down a level
			}
			else if ((temp.nextNodes.get(tempHeight).value.compareTo(tempElement) < 0)) //if value is still less, go to next node
			{
				temp = temp.nextNodes.get(tempHeight);
			}
		}
	}

	@Override
	public boolean removeAll(Collection<?> collec)
	{
		boolean rmvFlag = false;
		boolean tempFlag = false;
		for (Object i : collec) //for each object in collection
		{
			tempFlag = remove(i);
			if (tempFlag)
			{
				rmvFlag = true; //if at least one was removed, return true
			}
		}
		return rmvFlag;
	}

	@Override
	public boolean retainAll(Collection<?> collec)
	{
		clear(); //clear first
		for (Object i : collec) //for each object
		{
			add((T)i); //add it to the collection
		}
		return true;
	}

	@Override
	public int size()
	{ 
		return size - 2; //-2 accounts for head and tail, since a list with no values in it should return size 0.
	}

	@Override
	public Object[] toArray()
	{
		SkipListSetItem temp = head.nextNodes.get(0); //don't want to include head (or tail which is why loop has -2)
		Object arr[] = new Object[size - 2]; //makes new object array to be returned
		for (int i = 0; i < size - 2; i++)
		{
			arr[i] = temp.value;
			temp = temp.nextNodes.get(0);
		}
		return arr;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <E> E[] toArray(E[] a) 
	{
		SkipListSetItem temp = head.nextNodes.get(0); //don't want to include head (or tail which is why loop has -2)
		E newArr[] = (E[]) new Object[size - 2]; //make new array to be returned
		for (int i = 0; i < size - 2; i++)
		{
			newArr[i] = (E)temp.value;
			temp = temp.nextNodes.get(0);
		}
		return newArr; //returns new array instead of one in arguments
	}
	
	public void printSet() //just a print helper for testing purposes
	{
		SkipListSetItem temp = head;
		for (int i = 0; i < height; i++)
		{
			temp = head;
			for (int j = 0; j < size+1; j++)
			{
				System.out.println("Value is " + temp.value + ", height is " + temp.nodeHeight);
				temp = temp.nextNodes.get(i);
				if (temp == tail)
				{
					System.out.println("Value is " + temp.value + ", height is " + temp.nodeHeight);
					System.out.println("---END OF ROW---");
					break;
				}
			}
		}	
	}
	
	public void reBalance()
	{
		SkipListSetItem heightTemp = head.nextNodes.get(0); //used for making new heights
		SkipListSetItem relinkTemp = head; //used for relinking to new pointers
		SkipListSetItem searchTemp = head; //used for searching for new pointers
		SkipListSetItem reduceTemp = head; //used for reducing heights
		int newHeight;
		Random rand = new Random();
		
		for (int i = 0; i < size; i++) //this section of code reduces every node's height to 1 and unlinks all except for bottom level, makes a linked list
		{
			while (reduceTemp.nodeHeight > 1)
			{
				if (reduceTemp != head)
				{
					reduceTemp.prevNodes.remove(reduceTemp.nodeHeight - 1);
				}
				if (reduceTemp != tail)
				{
					reduceTemp.nextNodes.remove(reduceTemp.nodeHeight - 1);
				}
				reduceTemp.nodeHeight--;
			}
			if (reduceTemp == tail) //once at tail, end
			{
				break;
			}
			reduceTemp = reduceTemp.nextNodes.get(0);
		}
		
		head.nodeHeight = 1;
		tail.nodeHeight = 1;
		
		for (int i = 0; i < height - 1; i++) //re-initialize head and tail
		{
			head.nextNodes.add(tail);
			tail.prevNodes.add(head);
			head.nodeHeight++;
			tail.nodeHeight++;
		}
		
		for (int i = 0; i < size - 2; i++) //this section of code gives every node a new height (except head and tail)
		{
			newHeight = 0;
			while (true)
			{
				boolean randBool = rand.nextBoolean();
				newHeight++;
				if (randBool)
				{
					break;
				}
			}
			if (newHeight > height)
			{
				newHeight = height;
			}
			heightTemp.nodeHeight = newHeight;
			heightTemp = heightTemp.nextNodes.get(0);
		}
		
		for (int i = 1; i < height; i++) //relinks one row at a time
		{
			relinkTemp = head;
			for (int j = 0; j < size; j++) //goes to end of array
			{
				if (relinkTemp == tail) //if at tail, break
				{
					break;
				}
				if (relinkTemp.nodeHeight > i) //found a node that needs a new link at level i
				{
					searchTemp = relinkTemp; //finds what relinkTemp will link to
					for (int k = j; k < size; k++) //searches through list
					{
						searchTemp = searchTemp.nextNodes.get(0);
						if (searchTemp.nodeHeight > i) //if this node needs a new link at level i as well
						{
							if (relinkTemp == head) //if head, replace link so height stays the same
							{
								relinkTemp.nextNodes.set(i, searchTemp);
							}
							else //else, add it on
							{
								relinkTemp.nextNodes.add(searchTemp);
							}
							if (searchTemp == tail) //if tail, replace link so height stays the same
							{
								searchTemp.prevNodes.set(i, relinkTemp);
							}
							else //else, add it on
							{
								searchTemp.prevNodes.add(relinkTemp);
							}
							break;
						}
					}
					relinkTemp = searchTemp; //since searchTemp just had its prevNode linked, it now turns into node to have nextNode linked
				}
				else //if relinkTemp doesn't need new link, go to next node
				{
					relinkTemp = relinkTemp.nextNodes.get(0);
				}
			}
		}
		return;
	}

	@Override
	public Comparator<? super T> comparator() 
	{
		return null;
	}

	@Override
	public T first() //returns first non head node
	{
		return head.nextNodes.get(0).value;
	}

	@Override
	public T last() //returns first non tail node
	{
		return tail.prevNodes.get(0).value;
	}
	
	@Override
	public SortedSet<T> headSet(T toElement) 
	{
		throw new java.lang.UnsupportedOperationException();
	}
	
	@Override
	public SortedSet<T> subSet(T fromElement, T toElement) 
	{
		throw new java.lang.UnsupportedOperationException();
	}

	@Override
	public SortedSet<T> tailSet(T fromElement) {
		throw new java.lang.UnsupportedOperationException();
	}
}