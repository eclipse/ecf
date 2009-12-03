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

static jfieldID fieldHandle;
static jfieldID fieldResultCode;
static jfieldID fieldErrorCode;
static jfieldID fieldByteCount;
static jfieldID fieldServer;
static jfieldID fieldSyncObject;

#define NAMED_PIPE_RESULT "org/eclipse/ecf/ipc/fifo/FIFOResult"

void
FIFOResult_initialize(JNIEnv *env)
{
	jclass nprClass;

	if (methodsInitialized)
	{
		return;
	}
	else
	{
		methodsInitialized = 1;
	}

	nprClass = (*env)->FindClass(env, NAMED_PIPE_RESULT);

	fieldHandle = (*env)->GetFieldID(env, nprClass, "handle", "J");
	fieldResultCode = (*env)->GetFieldID(env, nprClass, "resultCode", "I");
	fieldErrorCode = (*env)->GetFieldID(env, nprClass, "errorCode", "I");
	fieldByteCount = (*env)->GetFieldID(env, nprClass, "byteCount", "I");
	fieldServer = (*env)->GetFieldID(env, nprClass, "server", "Z");
	fieldSyncObject = (*env)->GetFieldID(env, nprClass, "syncObject", "J");
}


void
FIFOResult_SetHandle(JNIEnv *env, jobject result, HANDLE handle)
{
	jlong value;
	DWORD dword;

	dword = (DWORD) handle;
	value = dword;
	(*env)->SetLongField(env, result, fieldHandle, value);
}


void
FIFOResult_SetResultCode(JNIEnv *env, jobject result, jint value)
{
	(*env)->SetIntField(env, result, fieldResultCode, value);
}


void
FIFOResult_SetErrorCode(JNIEnv *env, jobject result, DWORD errorCode)
{
	jint value;

	value = (jint) errorCode;
	(*env)->SetIntField(env, result, fieldErrorCode, value);
}


void
FIFOResult_SetByteCount(JNIEnv *env, jobject result, DWORD byteCount)
{
	jint value = (jint) byteCount;
	(*env)->SetIntField(env, result, fieldByteCount, value);
}

int
FIFOResult_isServer(JNIEnv *env, jobject result)
{
	jboolean value = (*env)->GetBooleanField(env, result, fieldServer);
	return (int) value;
}

jlong
FIFOResult_GetSyncObject(JNIEnv *env, jobject result)
{
	jlong value = (*env)->GetLongField(env, result, fieldSyncObject);
	return value;
}


void
FIFOResult_SetSyncObject(JNIEnv *env, jobject result, jlong value)
{
	(*env)->SetLongField(env, result, fieldSyncObject, value);
}

