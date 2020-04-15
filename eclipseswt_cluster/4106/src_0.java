package org.eclipse.swt.internal.image;

import java.io.*;
import org.eclipse.swt.*;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 1998, 2000  All Rights Reserved
 */

public class PngDecodingDataStream {
	PngIdatChunk currentChunk;
	PngChunkReader chunkReader;
	byte currentByte;
	int nextByteIndex;
	int nextBitIndex;
	
	PngLzBlockReader lzBlockReader;
	int adlerValue;
	
	static final int PRIME = 65521;
	static final int MAX_BIT = 7;		
	
PngDecodingDataStream(PngIdatChunk idatChunk, PngChunkReader chunkReader) {
	super();
	this.currentChunk = idatChunk;
	this.chunkReader = chunkReader;
	nextByteIndex = 0;
	nextBitIndex = MAX_BIT + 1;
	adlerValue = 1;
	lzBlockReader = new PngLzBlockReader(this);
	readCompressedDataHeader();
	lzBlockReader.readNextBlockHeader();
}

/**
 * This method should be called when the image decoder thinks
 * that all of the compressed image data has been read. This
 * method will ensure that the next data value is an end of 
 * block marker. If there are more blocks after this one,
 * the method will read them and ensure that they are empty.
 */
void assertImageDataAtEnd() {
	lzBlockReader.assertCompressedDataAtEnd();
}

int getNextIdatBits(int length) {
	int value = 0;
	for (int i = 0; i < length; i++) {
		value |= (getNextIdatBit() << i);
	}
	return value;
}

byte getNextIdatBit() {
	if (nextBitIndex > MAX_BIT) {
		currentByte = getNextIdatByte();
		nextBitIndex = 0;
	}	
	int mask = 1 << nextBitIndex;
	nextBitIndex++;
	return ((currentByte & mask) > 0) ? (byte) 1 : (byte) 0;	
}

private PngIdatChunk getNextChunk() {
	PngChunk chunk = chunkReader.readNextChunk();
	if (chunk == null) error();
	if (chunk.getChunkType() != PngChunk.CHUNK_IDAT) error(); 
	return (PngIdatChunk) chunk;
}

byte getNextIdatByte() {
	if (nextByteIndex > currentChunk.getLength() - 1) {
		currentChunk = getNextChunk();
		nextByteIndex = 0;
	}
	byte nextByte = currentChunk.getDataByteAtOffset(nextByteIndex);
	nextByteIndex++;
	nextBitIndex = MAX_BIT + 1;
	return nextByte;
}

private void updateAdler(byte value) {
	int low = adlerValue & 0xFFFF;
	int high = (adlerValue >> 16) & 0xFFFF;
	int valueInt = value & 0xFF;
	low = (low + valueInt) % PRIME;
	high = (low + high) % PRIME;
	adlerValue = (high << 16) | low;
}

byte getNextDecodedByte() {
	byte nextDecodedByte = lzBlockReader.getNextByte();
	updateAdler(nextDecodedByte);
	return nextDecodedByte;
}

void error() {
	SWT.error(SWT.ERROR_INVALID_IMAGE);
}

private void readCompressedDataHeader() {
	byte headerByte1 = getNextIdatByte();
	byte headerByte2 = getNextIdatByte();
	
	int number = (headerByte1 << 8) | (headerByte2 & 0xFF);
	if (number % 31 != 0) error();
	
	int compressionMethod = headerByte1 & 0x0F;
	if (compressionMethod != 8) error();
	
	int windowSizeHint = (headerByte1 & 0xF0) >> 4;
	if (windowSizeHint > 7) error();
	int windowSize = (1 << (windowSizeHint + 8));
	lzBlockReader.setWindowSize(windowSize);
	
	int dictionary = (headerByte2 & (1 << 5));
	if (dictionary != 0) error();
	
	int compressionLevel = (headerByte2 & 0xC0) >> 6;
}

void checkAdler() {
	int storedAdler = ((getNextIdatByte() & 0xFF) << 24)
		| ((getNextIdatByte() & 0xFF) << 16)
		| ((getNextIdatByte() & 0xFF) << 8)
		| (getNextIdatByte() & 0xFF);
	if (storedAdler != adlerValue) error();
}

}