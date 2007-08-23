package org.pentaho.di.core.row;

/**
 * 
 * We use this class to do row manipulations like add, delete, resize, etc.
 * That way, when we want to go for a metadata driven system with 
 * hiding deletes, oversized arrays etc, we can change these methods to find occurrences.
 * 
 * @author Matt
 *
 */
public class RowDataUtil {
	public static int OVER_ALLOCATE_SIZE = 10;

	/**
	 * Allocate a new Object array. However, over allocate by a constant factor to make adding values faster.
	 * 
	 * @param size the minimum size to allocate.
	 * @return the newly allocated object array
	 */
	public static Object[] allocateRowData(int size) {
		return new Object[size+OVER_ALLOCATE_SIZE];
	}

	/**
	 * Resize an object array making it bigger, over allocate, return the original array if there's enough room.
	 * 
	 * @param objects
	 * @param newSize
	 * @return A new object array, resized.
	 */
	public static Object[] resizeArray(Object[] objects, int newSize) {
		
		if (objects!=null && objects.length >= newSize)
			return objects;

		Object[] newObjects = new Object[newSize+OVER_ALLOCATE_SIZE];
		if (objects!=null) System.arraycopy(objects, 0, newObjects, 0, objects.length);
		return newObjects;
	}

	/**
	 * Remove an item from an Object array.  This is a slow operation, later we want to just flag this object and discard it at the next resize.
	 * The question is of-course if it makes that much of a difference in the end.
	 * 
	 * @param objects
	 * @param index
	 * @return
	 */
	public static Object[] removeItem(Object[] objects, int index) {
		Object[] newObjects = new Object[objects.length - 1];
		System.arraycopy(objects, 0, newObjects, 0, index);
		System.arraycopy(objects, index + 1, newObjects, index, objects.length - index - 1);
		return newObjects;
	}

	/**
	 * Add two arrays and make one new one.
	 * 
	 * @param one The first array
	 * @param the length of the row data or of it's longer, the location of the new extra value in the returned data row
	 * @param two The second array
	 * @return a new Array containing all elements from one and two after one another
	 */
	public static Object[] addRowData(Object[] one, int sourceLength, Object[] two) {
		Object[] result = resizeArray(one, sourceLength+two.length);

		System.arraycopy(two, 0, result, sourceLength, two.length);

		return result;
	}

	/**
	 * Add a single value to a row of data
	 * 
	 * @param rowData The original row of data
	 * @param the length of the row data or of it's longer, the location of the new extra value in the returned data row
	 * @param extra The extra value to add
	 * @return a new Array containing all elements, including the extra one
	 */
	public static Object[] addValueData(Object[] rowData, int length, Object extra) {
		
		Object[] result = resizeArray(rowData, length+1);
		result[length] = extra;
		return result;
	}

	/**
	 * Remove a number of items in a row of data.
	 * 
	 * @param rowData the row of data to remove from
	 * @param index the index of all the items in the source table to remove.  
	 *        We don't check if the same index gets deleted twice!
	 */
	public static Object[] removeItems(Object[] rowData, int[] index) {
		Object[] data = new Object[rowData.length - index.length];

		int count = data.length - 1;
		int removenr = index.length - 1;
		for (int i = rowData.length - 1; i >= 0; i--) {
			if (removenr >= 0 && i == index[removenr]) {
				removenr--;
			} else {
				data[count] = rowData[i];
				count--;
			}
		}

		return data;
	}
}