/*******************************************************************************
 * Copyright (c) 2009  Clark N. Hobbie
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Clark N. Hobbie - initial API and implementation
 *******************************************************************************/
/**
 * Provide native methods required by the FIFONative class.
 */


#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <stdio.h>
#include <unistd.h>
#include <errno.h>

#include "FIFOImpl.h"
#include "FIFOResult.h"


#define MAX_TIMEOUT 0x7FFFFFFF
#define HANDLE_TYPE int

static int methodsInitialized = 0;

static jmethodID methodIsCreator;
static jmethodID methodGetHandle;
static jmethodID methodGetActualName;
static jmethodID methodGetBufferSize;

static jmethodID methodDirection;

#define NAMED_PIPE_IMPL   "org/eclipse/ecf/ipc/fifo/FIFOImpl"
#define NAMED_PIPE_RESULT "org/eclipse/ecf/ipc/fifo/FIFOResult"


static void
implGetActualName(JNIEnv *env, jobject this, char* buf)
{
	jstring value;
	int length;

	value = (*env)->CallObjectMethod(env, this, methodGetActualName);
	length = (*env)->GetStringLength(env, value);
	(*env)->GetStringUTFRegion(env, value, 0, length, buf);
}

void FIFOImpl_initialize(JNIEnv *env)
{
	jclass npiClass;
	jclass nprClass;

	if (methodsInitialized)
	{
		return;
	}
	else
	{
		methodsInitialized = 1;
	}

	npiClass = (*env)->FindClass(env, NAMED_PIPE_IMPL);
	nprClass = (*env)->FindClass(env, NAMED_PIPE_RESULT);

	methodIsCreator = (*env)->GetMethodID(env, npiClass, "isCreator", "()Z");
	methodGetHandle = (*env)->GetMethodID(env, npiClass, "getHandle", "()J");
	methodGetActualName = (*env)->GetMethodID(env, npiClass, "getActualName", "()Ljava/lang/String;");
	methodGetBufferSize = (*env)->GetMethodID(env, npiClass, "getBufferSize", "()I");

	methodDirection = (*env)->GetMethodID(env, npiClass, "getDirectionInt", "()I");
}



static int
getDirection(JNIEnv *env, jobject this)
{
	jint value;

	value = (*env)->CallIntMethod(env, this, methodDirection);
	return (int) value;
}


JNIEXPORT jboolean JNICALL
Java_org_eclipse_ecf_ipc_fifopipe_FIFOImpl_virtualNameContainsActualNameImpl (
		JNIEnv *env,
		jclass class)
{
	return 0;
}


static int
implGetHandle(JNIEnv *env, jobject this)
{
	jlong value;

	value = (*env)->CallLongMethod(env, this, methodGetHandle);
	return (int) ((long) value);
}

/**
 * Connect the this process to a named pipe.
 *
 * On linux, a named pipe (fifo) is opened like any other file.  The only piece
 * of information needed is whether the process wants to read or to write the fifo.
 *
 * Note that, on linux, CLIPC *always* uses blocking mode when connecting to the
 * FIFO and non-blocking mode for reads and writes.  Non-blocking mode is used
 * for read/write even when the client is requesting blocking mode.
 */
JNIEXPORT void JNICALL
Java_org_eclipse_ecf_ipc_fifo_FIFOImpl_openImpl (
		JNIEnv *env,
		jobject this,
		jobject result,
		jint direction)
{
	int handle;
	int flags;
	char name[128];

	implGetActualName(env, this, name);
	switch (direction)
	{
		case org_eclipse_ecf_ipc_fifo_FIFOImpl_DIRECTION_WRITER :
			flags = O_WRONLY;
			break;

		case org_eclipse_ecf_ipc_fifo_FIFOImpl_DIRECTION_READER :
			flags = O_RDONLY;
			break;

		default :
			FIFOResult_SetResultCode(env, result, -1);
			return;
	}

	handle = open (name, flags);

	if (-1 == handle)
	{
		FIFOResult_SetResultCode(env, result, handle);
	}
	else
	{
		int ctl_result;

		ctl_result = fcntl(handle, F_SETFL, O_NDELAY);
		if (0 == ctl_result)
		{
			FIFOResult_SetResultCode(env, result, 0);
			FIFOResult_SetHandle(env, result, handle);
		}
		else
		{
			FIFOResult_SetResultCode(env, result, org_eclipse_ecf_ipc_fifo_FIFOResult_ERROR_CONNECT);
			FIFOResult_SetHandle(env, result, -1);
			FIFOResult_SetErrorCode(env, result, errno);
		}
	}
}


/**
 * Create a new instance of a named pipe and create the named pipe itself if it
 * doesn't already exist.
 *
 * In windows, the CreateFIFO system call must always be used before attempting
 * to perform any operations on a named pipe.  This is unlike POSIX, where the pipe
 * is created once, and then clients can simply open or close the pipe like they
 * would any other file.
 *
 * This method modifies the result parameter rather than returning a value.  The
 * various fields are populated according to whether or not the calls were successful.
 *
 * If the call was successful, result.resultCode should be 0 and result.handle
 * should contain the value returned by the CreateFIFO system call.
 *
 * If the call failed, result.resultCode should contain a non-zero value that is
 * taken from the constants defined by FIFOResult.ERROR_  In that situation,
 * the value of result.handle is not defined.
 */
JNIEXPORT void JNICALL
Java_org_eclipse_ecf_ipc_fifo_FIFOImpl_createImpl (
		JNIEnv *env,
		jobject this,
		jobject resultObject)
{
	int mode;
	int result;
	char name[512];

	mode = 0660;

	implGetActualName(env, this, name);

	result = mkfifo(name, mode);
	if (-1 == result && EEXIST == errno)
	{
		result = 0;
	}

	FIFOResult_SetResultCode(env, resultObject, result);
}


JNIEXPORT void JNICALL
Java_org_eclipse_ecf_ipc_fifo_FIFOImpl_writeImpl (
		JNIEnv *env,
		jobject this,
		jobject result,
		jbyteArray buffer,
		jint offset,
		jint length,
		jint timeoutMsec)
{
	jbyte *actualBuffer;
	jbyte *ptr;
	int returnValue;
	int handle;
	jboolean junk;


	handle = implGetHandle(env, this);

	Java_org_eclipse_ecf_ipc_fifo_FIFOImpl_select(env, this, result, timeoutMsec);

	if (FIFOResult_GetResultCode(env, result) != org_eclipse_ecf_ipc_fifo_FIFOResult_SUCCESS)
	{
		return;
	}


	actualBuffer = (*env)->GetByteArrayElements(env, buffer, &junk);
	ptr = actualBuffer + offset;

	returnValue = write(handle, ptr, length);
	(*env)->ReleaseByteArrayElements(env, buffer, actualBuffer, JNI_ABORT);

	if (-1 != returnValue)
	{
		FIFOResult_SetResultCode(env, result, 0);
		FIFOResult_SetByteCount(env, result, returnValue);
	}
	else
	{
		FIFOResult_SetResultCode(env, result, -1);
	}
}


JNIEXPORT void JNICALL
Java_org_eclipse_ecf_ipc_fifo_FIFOImpl_readImpl (
		JNIEnv *env,
		jobject this,
		jobject result,
		jbyteArray buffer,
		jint offset,
		jint length,
		jint timeoutMsec)
{
	jbyte *actualBuffer;
	jbyte *ptr;
	int returnValue;
	int handle;
	jboolean junk;

	handle = implGetHandle(env, this);

	actualBuffer = (*env)->GetByteArrayElements(env, buffer, &junk);
	ptr = actualBuffer + offset;

	Java_org_eclipse_ecf_ipc_fifo_FIFOImpl_select(env, this, result, timeoutMsec);

	returnValue = read(handle, ptr, length);

	(*env)->ReleaseByteArrayElements(env, buffer, actualBuffer, JNI_COMMIT);

	if (-1 != returnValue)
	{
		FIFOResult_SetResultCode(env, result, 0);
		FIFOResult_SetByteCount(env, result, returnValue);
	}
	else
	{
perror("error reading FIFO");
		FIFOResult_SetResultCode(env, result, -1);
	}
}


JNIEXPORT void JNICALL
Java_org_eclipse_ecf_ipc_fifo_FIFOImpl_select(
		JNIEnv *env,
		jobject this,
		jobject result,
		jint timeoutMsec)
{
	fd_set read, write, error;
	int handle;
	struct timeval timeout;
	struct timeval *ptimeout;
	int rvalue;


	if (timeoutMsec < 0)
	{
		ptimeout = NULL;
	}
	else
	{
		timeout.tv_sec = timeoutMsec % 1000;
		timeoutMsec = timeoutMsec - timeout.tv_sec;
		timeout.tv_usec = timeoutMsec * 1000;

		ptimeout = &timeout;
	}

	handle = implGetHandle(env, this);
	FD_ZERO(&read);
	FD_ZERO(&write);
	FD_ZERO(&error);


	FD_SET(handle, &error);

	switch(getDirection(env, this))
	{
		case org_eclipse_ecf_ipc_fifo_FIFOImpl_DIRECTION_READER :
			FD_SET(handle, &read);
			break;

		case org_eclipse_ecf_ipc_fifo_FIFOImpl_DIRECTION_WRITER :
			FD_SET(handle, &write);
			break;
	}

	rvalue = select(1 + handle, &read, &write, &error, NULL);

	if (0 == rvalue)
	{
		FIFOResult_SetResultCode(env, result, org_eclipse_ecf_ipc_fifo_FIFOResult_TIMEOUT);
		FIFOResult_SetErrorCode(env, result, 0);
	}
	else if (rvalue > 0)
	{
		FIFOResult_SetResultCode(env, result, org_eclipse_ecf_ipc_fifo_FIFOResult_SUCCESS);
		FIFOResult_SetErrorCode(env, result, 0);
	}
	else
	{
		FIFOResult_SetResultCode(env, result, org_eclipse_ecf_ipc_fifo_FIFOResult_ERROR_SELECT);
		FIFOResult_SetErrorCode(env, result, errno);
	}
}

/**
 * Return the actual name for for a "simple" virtual name.
 *
 * If the platform has naming requirements for FIFOs, then return an actual name
 * for a short file name.
 *
 * If the platform has no naming requirements, then return null.
 *
 * On linux, there are no naming requirements, so return null.
 */
JNIEXPORT jstring JNICALL
Java_org_eclipse_ecf_ipc_fifo_FIFOImpl_toActualName (
		JNIEnv *env,
		jclass class,
		jstring name)
{
	return NULL;
}

