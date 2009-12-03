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

#include <windows.h>
#include <stdio.h>
#include "FIFOImpl.h"
#include "FIFOResult.h"
#include "FIFOResult.h"


#define MAX_TIMEOUT 0x7FFFFFFF
#define HANDLE_TYPE int

static int methodsInitialized = 0;

static jmethodID methodIsCreator;
static jmethodID methodGetHandle;
static jmethodID methodGetActualName;
static jmethodID methodGetBufferSize;

#define NAMED_PIPE_IMPL   "org/eclipse/ecf/ipc/fifo/FIFOImpl"
#define NAMED_PIPE_RESULT "org/eclipse/ecf/ipc/fifo/FIFOResult"


static HANDLE
getSyncHandle(JNIEnv *env, jobject result)
{
	DWORD temp;

	temp = (DWORD) FIFOResult_GetSyncObject(env, result);
	return (HANDLE) temp;
}


static void
setSyncHandle(JNIEnv *env, jobject result, HANDLE value)
{
	DWORD temp;

	temp = (DWORD) value;
	FIFOResult_SetSyncObject(env, result, (jlong) temp);
}


static void
convertToTSTR(const char* s, TCHAR *buf)
{
	int index;

	index = 0;
	while (s[index] != '\0')
	{
		buf[index] = s[index];
		index++;
	}

	buf[index] = '\0';
}


static void
jstringToTString(JNIEnv *env, jstring s, TCHAR *buff)
{
	int len;
	char localBuffer[256];


	len = (*env)->GetStringLength(env, s);
	(*env)->GetStringUTFRegion(env, s, 0, len, localBuffer);
	convertToTSTR(localBuffer, buff);
}


static void
jstringToString(JNIEnv *env, jstring s, char *buff)
{
	int len;

	len = (*env)->GetStringLength(env, s);
	(*env)->GetStringUTFRegion(env, s, 0, len, buff);
}

static void
implGetActualName(JNIEnv *env, jobject this, TCHAR *buf)
{
	jstring value;

	value = (*env)->CallObjectMethod(env, this, methodGetActualName);
	jstringToTString(env, value, buf);
}

static DWORD
implGetBufferSize(JNIEnv *env, jobject this)
{
	jint value;
	long temp;

	value = (*env)->CallIntMethod(env, this, methodGetBufferSize);
	temp = (long) value;
	return (DWORD) temp;
}

void
FIFOImpl_initialize(JNIEnv *env)
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
}




JNIEXPORT jboolean JNICALL
Java_org_eclipse_ecf_ipc_fifo_FIFOImpl_virtualNameContainsActualNameImpl (
		JNIEnv *env,
		jclass class)
{
	jboolean result = 1;
	return result;
}


static int
implIsCreator(JNIEnv *env, jobject this)
{
	jint value;

	value = (*env)->CallBooleanMethod(env, this, methodIsCreator);
	return (int) value;
}

static HANDLE
implGetHandle(JNIEnv *env, jobject this)
{
	jlong value;

	value = (*env)->CallLongMethod(env, this, methodGetHandle);
	return (HANDLE) ((long) value);
}


void
openServer(JNIEnv *env, jobject this, jobject result)
{
	HANDLE nph;
	BOOL success;
	jint resultCode;


	nph = implGetHandle(env, this);
	success = ConnectNamedPipe(nph, NULL);

	if (success || ERROR_PIPE_CONNECTED == GetLastError())
	{
		resultCode = org_eclipse_ecf_ipc_fifo_FIFOResult_SUCCESS;
		FIFOResult_SetHandle(env, result, nph);
	}
	else
	{
		switch(GetLastError())
		{
			case ERROR_NO_DATA :
			{
				resultCode = org_eclipse_ecf_ipc_fifo_FIFOResult_ERROR_INVALID_HANDLE;
				break;
			}

			default :
			{
				resultCode = org_eclipse_ecf_ipc_fifo_FIFOResult_ERROR_UNKNOWN;
				break;
			}
		}
	}

	FIFOResult_SetResultCode(env, result, resultCode);
}


void
openClient (JNIEnv *env, jobject this, jobject result)
{
	HANDLE handle;
	TCHAR name[256];
	DWORD readWriteMode;
	jint resultCode;
	DWORD errorCode;

	readWriteMode = PIPE_ACCESS_DUPLEX | PIPE_TYPE_BYTE | PIPE_WAIT;
	implGetActualName(env, this, name);

	handle = CreateFile(
         name,			// pipe name
         readWriteMode,	// read and write access
         0,				// no sharing
         NULL,			// default security attributes
         OPEN_EXISTING,	// opens existing pipe
         0,				// default attributes
         NULL);			// no template file

	if (INVALID_HANDLE_VALUE != handle)
	{
		resultCode = 0;
		errorCode = 0;
		FIFOResult_SetHandle(env, result, handle);
	}
	else
	{
		errorCode = GetLastError();
		switch(GetLastError())
		{
			case ERROR_ACCESS_DENIED :
				resultCode = (jint) org_eclipse_ecf_ipc_fifo_FIFOResult_ERROR_ACCESS_DENIED;
				break;

			case ERROR_PIPE_BUSY :
				resultCode = (jint) org_eclipse_ecf_ipc_fifo_FIFOResult_ERROR_PIPE_BUSY;
				break;

			default :
				resultCode = (jint) org_eclipse_ecf_ipc_fifo_FIFOResult_ERROR_UNKNOWN;
				break;
		}
	}

	FIFOResult_SetResultCode(env, result, resultCode);
	FIFOResult_SetErrorCode(env, result, errorCode);
}


/**
 * Connect the named pipe to another process.
 *
 * Windows requires that the connectiong process know if it is the "client" or the
 * "server" when connecting, so we care about the creator parameter.
 *
 * All Windows named pipes, however, are bidirectional, so ignore the direction
 * parameter.
 */
JNIEXPORT void JNICALL
Java_org_eclipse_ecf_ipc_fifo_FIFOImpl_openImpl (
		JNIEnv *env,
		jobject this,
		jobject result,
		jint direction)
{
	int isCreatorValue;
	HANDLE handle;

	isCreatorValue = implIsCreator(env, this);
	handle = implGetHandle(env, this);

	if (isCreatorValue)
	{
		openServer(env, this, result);
	}
	else
	{
		openClient(env, this, result);
	}
}


/**
 * Create a new instance of a named pipe and create the named pipe itself if it
 * doesn't already exist.
 *
 * In windows, the CreateNamedPipe system call must always be used before attempting
 * to perform any operations on a named pipe.  This is unlike POSIX, where the pipe
 * is created once, and then clients can simply open or close the pipe like they
 * would any other file.
 *
 * This method modifies the result parameter rather than returning a value.  The
 * various fields are populated according to whether or not the calls were successful.
 *
 * If the call was successful, result.resultCode should be 0 and result.handle
 * should contain the value returned by the CreateNamedPipe system call.
 *
 * If the call failed, result.resultCode should contain a non-zero value that is
 * taken from the constants defined by FIFOResult.ERROR_  In that situation,
 * the value of result.handle is not defined.
 */
JNIEXPORT void JNICALL
Java_org_eclipse_ecf_ipc_fifo_FIFOImpl_createImpl (
		JNIEnv *env,
		jobject this,
		jobject result)
{
	jint resultCodeValue;
	HANDLE handle;
	DWORD dwOpenMode;
	TCHAR tstring[256];
	DWORD bufferSize;
	DWORD dwPipeMode;
	DWORD nDefaultTimeout;

	dwOpenMode = PIPE_ACCESS_DUPLEX;
	dwPipeMode = PIPE_TYPE_BYTE | PIPE_READMODE_BYTE | PIPE_WAIT;
	nDefaultTimeout = NMPWAIT_USE_DEFAULT_WAIT;

	implGetActualName(env, this, tstring);
	bufferSize = implGetBufferSize(env, this);


	handle = CreateNamedPipe(
		tstring,					// pipe name
		dwOpenMode,					// open modes
		dwPipeMode,					// pipe mode (stream oriented, blocking)
		PIPE_UNLIMITED_INSTANCES,	// create as many as you want
		(DWORD) bufferSize,			// outgoing buffer size
		(DWORD) bufferSize,			// incoming buffer size
		nDefaultTimeout,			// no default timeout
		NULL);						// default security

	if (INVALID_HANDLE_VALUE != handle)
	{
		resultCodeValue = 0;
		FIFOResult_SetHandle(env, result, handle);
	}
	else
	{
		if (ERROR_ACCESS_DENIED == GetLastError())
		{
			resultCodeValue = (jint) org_eclipse_ecf_ipc_fifo_FIFOResult_ERROR_ACCESS_DENIED;
		}
		else
		{
			resultCodeValue = (jint) org_eclipse_ecf_ipc_fifo_FIFOResult_ERROR_UNKNOWN;
		}
	}

	FIFOResult_SetResultCode(env, result, resultCodeValue);
}


JNIEXPORT void JNICALL
Java_org_eclipse_ecf_ipc_fifo_FIFOImpl_writeImpl (
		JNIEnv *env,
		jobject this,
		jobject result,
		jbyteArray buffer,
		jint offset,
		jint length)
{
	BOOL success;
	jboolean isCopy;
	HANDLE handle;
	jbyte *actualBuffer;
	DWORD actualCount;
	jbyte *bptr;

	handle = implGetHandle(env, this);

	actualBuffer = (*env)->GetByteArrayElements(env, buffer, &isCopy);
	bptr = actualBuffer + offset;

	success = WriteFile(handle, bptr, length, &actualCount, NULL);

	(*env)->ReleaseByteArrayElements(env, buffer, actualBuffer, JNI_ABORT);

	FIFOResult_SetHandle(env, result, handle);
	if (success)
	{
		FIFOResult_SetResultCode(env, result, 0);
		FIFOResult_SetErrorCode(env, result, 0);
		FIFOResult_SetByteCount(env, result, actualCount);
	}
	else
	{
		FIFOResult_SetResultCode(env, result, -1);
		FIFOResult_SetErrorCode(env, result, GetLastError());
	}
}


JNIEXPORT void JNICALL
Java_org_eclipse_ecf_ipc_fifo_FIFOImpl_readImpl (
		JNIEnv *env,
		jobject this,
		jobject result,
		jbyteArray buffer,
		jint offset,
		jint length)
{
	BOOL success;
	jboolean junk;
	HANDLE handle;
	jbyte *actualBuffer;
	jbyte *ptr;
	DWORD actualCount;

	FIFOResult_initialize(env);

	handle = implGetHandle(env, this);

	actualBuffer = (*env)->GetByteArrayElements(env, buffer, &junk);
	ptr = actualBuffer + offset;

	success = ReadFile(handle, ptr, length, &actualCount, NULL);

	(*env)->ReleaseByteArrayElements(env, buffer, actualBuffer, JNI_COMMIT);

	FIFOResult_SetHandle(env, result, handle);
	if (success)
	{
		FIFOResult_SetResultCode(env, result, 0);
		FIFOResult_SetErrorCode(env, result, 0);
		FIFOResult_SetByteCount(env, result, actualCount);
	}
	else
	{
		FIFOResult_SetResultCode(env, result, -1);
		FIFOResult_SetErrorCode(env, result, GetLastError());
	}
}

void createNonBlockingServer(
		JNIEnv *env,
		jobject result)
{
    jint resultCodeValue;
    HANDLE handle;
    DWORD dwOpenMode;
    TCHAR tstring[256];
    DWORD dwPipeMode;
    dwOpenMode = PIPE_ACCESS_DUPLEX;
    dwOpenMode = dwOpenMode | FILE_FLAG_OVERLAPPED;
    dwPipeMode = PIPE_TYPE_BYTE;
    dwPipeMode = dwPipeMode | PIPE_WAIT;

    handle = CreateNamedPipe(
		tstring,					// pipe name
		dwOpenMode,					// open modes
		dwPipeMode,					// pipe mode (stream oriented, blocking)
		PIPE_UNLIMITED_INSTANCES,	// create as many as you want
		(DWORD) 1024,				// outgoing buffer size
		(DWORD) 1024,				// incoming buffer size
		NMPWAIT_USE_DEFAULT_WAIT,	// no default timeout
		NULL);

    if (INVALID_HANDLE_VALUE != handle)
	{
		resultCodeValue = 0;
		FIFOResult_SetHandle(env, result, handle);
	}
	else if (ERROR_ACCESS_DENIED == GetLastError())
	{
		resultCodeValue = (jint) org_eclipse_ecf_ipc_fifo_FIFOResult_ERROR_ACCESS_DENIED;
	}
	else
	{
		resultCodeValue = (jint) org_eclipse_ecf_ipc_fifo_FIFOResult_ERROR_UNKNOWN;
	}

    FIFOResult_SetResultCode(env, result, resultCodeValue);
}

static void
closeNonBlockingError(JNIEnv *env, jobject result, int resultCode)
{
	FIFOResult_SetErrorCode(env, result, GetLastError());
	FIFOResult_SetResultCode(env, result, (jint) org_eclipse_ecf_ipc_fifo_FIFOResult_ERROR_CONNECT);
	CloseHandle(getSyncHandle(env, result));
}

static void
connectNonBlockingServer(JNIEnv *env, jobject this, jobject result)
{
	HANDLE handle;
	BOOL success;
	OVERLAPPED over;
	DWORD waitResult;

	over.Offset = 0;
	over.OffsetHigh = 0;
	over.hEvent = CreateEvent(NULL, TRUE, TRUE, "pipeReady");

	//
	// Note that the connect call SHOULD FAIL at this point.
	//
	handle = implGetHandle(env, this);
	success = ConnectNamedPipe(handle, &over);

	if (success || ERROR_IO_PENDING != GetLastError())
	{
		closeNonBlockingError(env, result, org_eclipse_ecf_ipc_fifo_FIFOResult_ERROR_CONNECT);
		return;
	}

	//
	// Despite being non-blocking, the caller will wait indefinitely for someone
	// to connect
	//
	waitResult = WaitForSingleObject(over.hEvent, INFINITE);
	if (WAIT_OBJECT_0 != waitResult)
	{
		closeNonBlockingError(env, result, org_eclipse_ecf_ipc_fifo_FIFOResult_ERROR_CONNECT);
		return;
	}

	//
	// otherwise, we now have a connection
	//
	FIFOResult_SetErrorCode(env, result, 0);
	FIFOResult_SetResultCode(env, result, 0);
	FIFOResult_SetHandle(env, result, handle);
	setSyncHandle(env, result, over.hEvent);
}

static void
connectNonBlockingClient (
		JNIEnv *env,
		jobject this,
		jobject result)
{
	HANDLE file;
	TCHAR actual[512];
	DWORD mode;

	mode = GENERIC_READ | GENERIC_WRITE;

	implGetActualName(env, this, actual);

	file = CreateFile(
		actual,			// name
		mode,			// easiest to just to read/write
		0,				// no sharing
		NULL,			// default security
		OPEN_EXISTING,	// open existing pipe
		FILE_FLAG_OVERLAPPED,	// async I/O
		NULL);			// no template file

	if (INVALID_HANDLE_VALUE == file)
	{
		closeNonBlockingError(env, result, org_eclipse_ecf_ipc_fifo_FIFOResult_ERROR_CONNECT);
		return;
	}
	else
	{
		HANDLE eventObj;
		eventObj = CreateEvent(NULL, TRUE, TRUE, "pipeReady");
		setSyncHandle(env, result, eventObj);
		FIFOResult_SetErrorCode(env, result, 0);
		FIFOResult_SetResultCode(env, result, 0);
		FIFOResult_SetHandle(env, result, file);
		setSyncHandle(env, result, eventObj);
	}
}

JNIEXPORT void JNICALL
Java_org_eclipse_ecf_ipc_fifo_FIFOImpl_connectNonBlocking (
		JNIEnv *env,
		jobject this,
		jobject result)
{
	 if (FIFOResult_isServer(env, result))
	 {
		 connectNonBlockingServer(env, this, result);
	 }
	 else
	 {
		 connectNonBlockingClient(env, this, result);
	 }
}

JNIEXPORT void JNICALL
Java_org_eclipse_ecf_ipc_fifo_FIFOImpl_createNonBlocking (
		JNIEnv *env,
		jclass class,
		jobject result,
		jstring name)
{
	HANDLE eventHandle;

	if (FIFOResult_isServer(env, result))
	{
		createNonBlockingServer(env, result);
	}

	eventHandle = CreateEvent(NULL, TRUE, TRUE, "pipeReady");

	setSyncHandle(env, result, eventHandle);
}

static void
performNonBlockingRead(
		JNIEnv *env,
		jobject this,
		jobject result,
		VOID* ptr,
		DWORD length,
		DWORD timeoutMsec)
{
	OVERLAPPED over;
	DWORD bytesRead;
	BOOL success;

	over.Offset = 0;
	over.OffsetHigh = 0;
	over.hEvent = getSyncHandle(env, result);


	success = ReadFile(
		implGetHandle(env, this),
		ptr,
		(DWORD) length,
		&bytesRead,
		&over);

	if (success || GetLastError() != ERROR_IO_PENDING)
	{
		FIFOResult_SetResultCode(env, result, org_eclipse_ecf_ipc_fifo_FIFOResult_ERROR_READ);
		FIFOResult_SetErrorCode(env, result, GetLastError());
		return;
	}

	DWORD resultCode;
	DWORD timeout;

	if (timeoutMsec < 0)
	{
		timeout = INFINITE;
	}

	resultCode = WaitForSingleObject(
			getSyncHandle(env, result),
			(DWORD) timeoutMsec);

	if (WAIT_TIMEOUT == resultCode)
	{
		FIFOResult_SetResultCode(env, result, org_eclipse_ecf_ipc_fifo_FIFOResult_TIMEOUT);
		FIFOResult_SetErrorCode(env, result, GetLastError());
		return;
	}
	else if (WAIT_OBJECT_0 != resultCode)
	{
		FIFOResult_SetResultCode(env, result, org_eclipse_ecf_ipc_fifo_FIFOResult_ERROR_READ);
		FIFOResult_SetErrorCode(env, result, GetLastError());
		return;
	}

	GetOverlappedResult(
			implGetHandle(env, this),
			&over,
			&bytesRead,
			FALSE);

	FIFOResult_SetResultCode(env, result, org_eclipse_ecf_ipc_fifo_FIFOResult_SUCCESS);
	FIFOResult_SetErrorCode(env, result, ERROR_SUCCESS);
	FIFOResult_SetByteCount(env, result, bytesRead);
}

JNIEXPORT void JNICALL
Java_org_eclipse_ecf_ipc_fifo_FIFOImpl_readNonBlocking (
		JNIEnv *env,
		jobject this,
		jobject result,
		jbyteArray buffer,
		jint start,
		jint length,
		jint timeoutMsec)
{
	VOID *actualBuffer;
	VOID *ptr;

	actualBuffer = (*env)->GetByteArrayElements(env, buffer, NULL);
	ptr = actualBuffer + start;

	performNonBlockingRead(env, this, result, ptr, length, timeoutMsec);

	(*env)->ReleaseByteArrayElements(env, buffer, actualBuffer, JNI_COMMIT);
}


static void
nonBlockingWrite(
		JNIEnv *env,
		jobject this,
		jobject result,
		VOID* ptr,
		DWORD length,
		DWORD timeoutMsec)
{
	OVERLAPPED over;
	DWORD bytesWritten;
	BOOL success;
	DWORD resultCode;


	over.Offset = 0;
	over.OffsetHigh = 0;
	over.hEvent = getSyncHandle(env, result);

	success = WriteFile(
		implGetHandle(env, this),
		ptr,
		(DWORD) length,
		&bytesWritten,
		&over);

	if (success || GetLastError() != ERROR_IO_PENDING)
	{
		FIFOResult_SetResultCode(env, result, org_eclipse_ecf_ipc_fifo_FIFOResult_ERROR_READ);
		FIFOResult_SetErrorCode(env, result, GetLastError());
		return;
	}

	if (timeoutMsec < 0)
	{
		timeoutMsec = INFINITE;
	}

	resultCode = WaitForSingleObject(
			getSyncHandle(env, result),
			timeoutMsec);

	if (WAIT_TIMEOUT == resultCode)
	{
		FIFOResult_SetResultCode(env, result, org_eclipse_ecf_ipc_fifo_FIFOResult_TIMEOUT);
		FIFOResult_SetErrorCode(env, result, GetLastError());
		return;
	}
	else if (WAIT_OBJECT_0 != resultCode)
	{
		FIFOResult_SetResultCode(env, result, org_eclipse_ecf_ipc_fifo_FIFOResult_ERROR_READ);
		FIFOResult_SetErrorCode(env, result, GetLastError());
		return;
	}

	GetOverlappedResult(
			implGetHandle(env, this),
			&over,
			&bytesWritten,
			FALSE);

	FIFOResult_SetResultCode(env, result, org_eclipse_ecf_ipc_fifo_FIFOResult_SUCCESS);
	FIFOResult_SetErrorCode(env, result, ERROR_SUCCESS);
	FIFOResult_SetByteCount(env, result, bytesWritten);
}

JNIEXPORT void JNICALL
Java_org_eclipse_ecf_ipc_fifo_FIFOImpl_writeNonBlocking (
		JNIEnv *env,
		jobject this,
		jobject result,
		jbyteArray buffer,
		jint start,
		jint length,
		jint timeoutMsec)
{
	VOID *actualBuffer;
	VOID *ptr;

	actualBuffer = (*env)->GetByteArrayElements(env, buffer, NULL);
	ptr = actualBuffer + start;

	nonBlockingWrite(env, this, result, ptr, length, timeoutMsec);

	(*env)->ReleaseByteArrayElements(env, buffer, actualBuffer, JNI_COMMIT);
}

JNIEXPORT jstring JNICALL
Java_org_eclipse_ecf_ipc_fifo_FIFOImpl_toActualName(
		JNIEnv *env,
		jclass class,
		jstring suffix)
{
	char strSuffix[512];
	char buffer[512];


	jstringToString(env, suffix, strSuffix);
	sprintf(buffer, "\\\\.\\pipe\\%s", strSuffix);
	return (*env)->NewStringUTF(env, buffer);
}
