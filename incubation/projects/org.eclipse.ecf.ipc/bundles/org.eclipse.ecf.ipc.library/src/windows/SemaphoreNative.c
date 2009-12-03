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
#include <windows.h>
#include <stdio.h>
#include "SemaphoreNative.h"
#include "SemaphoreResult.h"

static int errorToReturnCode();

#define MAX_TIMEOUT 0x7FFFFFFF
#define HANDLE_TYPE int

JNIEXPORT void JNICALL Java_org_eclipse_ecf_ipc_semaphore_SemaphoreNative_connect(
		JNIEnv *env,
		jclass clazz,
		jobject resultObject,
		jstring name,
		jint initialValue)
{
    HANDLE sem;
    const char *s;
    LONG msInitialValue, msMaxValue;

    msInitialValue = (LONG) initialValue;
    s = (*env)->GetStringUTFChars(env, name, NULL);

    sem = CreateSemaphore(
		NULL,			// default security attributes
		msInitialValue,	// initial value
		msMaxValue,		// max value
		s				// name
	);

    (*env)->ReleaseStringUTFChars(env, name, s);

    if (NULL == sem)
    {
    	int errorCode = errorToReturnCode();
        SemaphoreResult_setResultCode(env, resultObject, errorCode);
        SemaphoreResult_setErrorCode(env, resultObject, GetLastError());
        sem = (HANDLE) -1;
        SemaphoreResult_setHandle(env, resultObject, (int) sem);
    }
    else
    {
    	SemaphoreResult_setResultCode(env, resultObject, org_eclipse_ecf_ipc_semaphore_SemaphoreNative_RESULT_SUCCESS);
    	SemaphoreResult_setErrorCode(env, resultObject, 0);
    	SemaphoreResult_setHandle(env, resultObject, (int) sem);
    }
}

/*
 * Convert a Windows error code returned from GetErrorCode into something that
 * is defined by our interface.
 */
static int errorToReturnCode()
{
    DWORD errorCode;
    int returnCode;

    errorCode = GetLastError();

    switch(errorCode)
    {
		case ERROR_SUCCESS :
			returnCode = org_eclipse_ecf_ipc_semaphore_SemaphoreNative_RESULT_SUCCESS;
			break;

		case ERROR_ACCESS_DENIED :
			returnCode = org_eclipse_ecf_ipc_semaphore_SemaphoreNative_RESULT_ACCESS_DENIED;
			break;

		case ERROR_INVALID_HANDLE :
			returnCode = org_eclipse_ecf_ipc_semaphore_SemaphoreNative_RESULT_UNKNOWN_HANDLE;
			break;

		case ERROR_SEM_TIMEOUT :
			returnCode = org_eclipse_ecf_ipc_semaphore_SemaphoreNative_RESULT_TIMEOUT;
			break;

		case ERROR_TOO_MANY_POSTS :
			returnCode = org_eclipse_ecf_ipc_semaphore_SemaphoreNative_RESULT_TOO_MANY_INCREMENTS;
			break;

		default :
			returnCode = org_eclipse_ecf_ipc_semaphore_SemaphoreNative_RESULT_UNKNOWN_ERROR;
			break;
    }

    return returnCode;
}

JNIEXPORT void JNICALL Java_org_eclipse_ecf_ipc_semaphore_SemaphoreNative_increment(
		JNIEnv *env,
		jclass this,
		jobject resultObject,
		jlong sem
)
{
    HANDLE handle;
    BOOL result;
	long previous;

    //
	// The compiler will whine about how we're converting an integer to a
	// pointer, and that the pointer size (32 bits) is different from the
	// integer size (64 bits).  This makes it hard to find the real errors in
	// the output, hence this bit of code.
    //
    handle = (HANDLE) ((HANDLE_TYPE)sem);
    result = ReleaseSemaphore(handle, 1, &previous);
}


/*
 * Class:     org_eclipse_ecf_ipc_semaphore_SemaphoreNative
 * Method:    decrement
 * Signature: (JJJ)I
 *
 * The timeout is the time we should wait if the semaphore is not available.
 *
 * The timeout is in nanoseconds.
 */
JNIEXPORT void JNICALL Java_org_eclipse_ecf_ipc_semaphore_SemaphoreNative_decrement(
		JNIEnv *env,
		jclass clazz,
		jobject resultObject,
		jlong sem,
		jlong timeout
)
{
    DWORD result, msec;
    HANDLE handle;
    int code;

	if (0 > timeout || timeout > MAX_TIMEOUT)
	{
		msec = INFINITE;
	}
	else
	{
		msec = timeout;
	}

    //
    // The compiler will whine about how we're converting an integer to a
    // pointer, and that the pointer size (32 bits) is different from the
    // integer size (64 bits).  This makes it hard to find the real errors in
    // the output, hence this bit of code.
    //
    handle = (HANDLE) ((int) sem);

    result = WaitForSingleObject(handle, msec);

    switch (result)
    {
		case WAIT_OBJECT_0 :
			code = org_eclipse_ecf_ipc_semaphore_SemaphoreNative_RESULT_SUCCESS;
			break;

		case WAIT_TIMEOUT :
			code = org_eclipse_ecf_ipc_semaphore_SemaphoreNative_RESULT_TIMEOUT;
			break;

		case WAIT_ABANDONED :
			code = org_eclipse_ecf_ipc_semaphore_SemaphoreNative_RESULT_PLATFORM_ERROR;
			break;

		case WAIT_FAILED :
			code = errorToReturnCode();
			break;

		default :
			code = org_eclipse_ecf_ipc_semaphore_SemaphoreNative_RESULT_UNKNOWN_ERROR;
			break;
    }

    SemaphoreResult_setErrorCode(env, resultObject, GetLastError());
    SemaphoreResult_setHandle(env, resultObject, (int) handle);
    SemaphoreResult_setResultCode(env, resultObject, code);
}


JNIEXPORT jboolean JNICALL Java_org_eclipse_ecf_ipc_semaphore_SemaphoreNative_supportsGetValue
  (JNIEnv *env, jclass this)
{
    return JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_org_eclipse_ecf_ipc_semaphore_SemaphoreNative_supportsSetValue
  (JNIEnv *env, jclass this)
{
    return JNI_FALSE;
}


/*
 * Class:     org_eclipse_ecf_ipc_semaphore_SemaphoreNative
 * Method:    createResult
 * Signature: ()Lorg/eclipse/ecf/ipc/SemaphoreNative/ConnectResult;
 */
JNIEXPORT void JNICALL Java_org_eclipse_ecf_ipc_semaphore_SemaphoreNative_createResult
  (JNIEnv *env, jclass this, jobject obj, jstring name, jint maxValue, jint initialValue)
{
}

JNIEXPORT void JNICALL Java_org_eclipse_ecf_ipc_semaphore_SemaphoreNative_linkTest
  (JNIEnv *env, jclass this)
{
	return;
}

JNIEXPORT jint JNICALL Java_org_eclipse_ecf_ipc_semaphore_SemaphoreNative_getNamingMethod
  (JNIEnv *env, jclass this)
{
	return org_eclipse_ecf_ipc_semaphore_SemaphoreNative_METHOD_INTEGER;
}



