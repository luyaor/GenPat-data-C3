package org.eclipse.swt.dnd;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved
 */

import org.eclipse.swt.internal.Converter;
/**
 * The <code>TextTransfer</code> class is used to transfer text in a drag and drop operation.
 */
public class TextTransfer extends ByteArrayTransfer {

	private static TextTransfer _instance = new TextTransfer();
	private static final String TYPENAME1 = "STRING";
	private static final int TYPEID1 = registerType(TYPENAME1);
	private static final String TYPENAME2 = "text/plain";
	private static final int TYPEID2 = registerType(TYPENAME2);
	private static final String TYPENAME3 = "text/text";
	private static final int TYPEID3 = registerType(TYPENAME3);
	private static final String TYPENAME4 = "TEXT";
	private static final int TYPEID4 = registerType(TYPENAME4);

private TextTransfer() {
}
/**
 * Returns the singleton instance of the TextTransfer class.
 *
 * @return the singleton instance of the TextTransfer class
 */
public static TextTransfer getInstance () {
	return _instance;
}
/**
 * Converts a plain text Java String to a platform specific representation. 
 * <p>
 * On a successful conversion, the transferData.result field will be set as follows:
 * <ul>
 * <li>Windows: OLE.S_OK
 * <li>Motif: 1
 * <li>GTK: 1
 * </ul>
 * If this transfer agent is unable to perform the conversion,
 * the transferData.result field will be set to a failure value as follows:
 * <ul>
 * <li>Windows: OLE.DV_E_TYMED
 * <li>Motif: 0
 * <li>GTK: 0
 * </ul></p>
 *
 * @param object a Java String containing the data to be transferred
 * @param transferData an empty TransferData object; this object will be filled in on return
 *        with the platform specific format of the data
 */
public void javaToNative (Object object, TransferData transferData){
	if (object == null || !(object instanceof String)) return;
	byte [] buffer = Converter.wcsToMbcs (null, (String)object, true);
	super.javaToNative(buffer, transferData);
}
/**
 * Converts a platform specific representation of a string to a Java String.
 *
 * @param transferData the platform specific representation of the data that has been transferred
 * @return a Java String containing the transferred data if the conversion was successful;
 *         otherwise null
 */
public Object nativeToJava(TransferData transferData){
	// get byte array from super
	byte[] buffer = (byte[])super.nativeToJava(transferData);
	if (buffer == null) return null;
	// convert byte array to a string
	char [] unicode = Converter.mbcsToWcs (null, buffer);
	return new String (unicode);
}
protected String[] getTypeNames(){
	return new String[]{TYPENAME1, TYPENAME2, TYPENAME3, TYPENAME4};
}
protected int[] getTypeIds(){
	return new int[]{TYPEID1, TYPEID2, TYPEID3, TYPEID4};
}
}
