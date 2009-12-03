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
/*
 * SemaphoreResult.c
 *
 * Accessor methods for the SemaphoreResult class.
 *
 *  Created on: Mar 5, 2009
 *      Author: cnh
 */

#include <windows.h>
#include "SemaphoreResult.h"

static jfieldID resultCodeField;
static jfieldID handleField;
static jfieldID errorCodeField;

#define CLASS_NAME "org/eclipse/ecf/ipc/semaphore/SemaphoreResult"

void SemaphoreResult_initialize(JNIEnv *env)
{
	jclass clazz;

	clazz = (*env)->FindClass(env, CLASS_NAME);

	handleField = (*env)->GetFieldID(env, clazz, "handle", "J");
	errorCodeField = (*env)->GetFieldID(env, clazz, "errorCode", "I");
	resultCodeField = (*env)->GetFieldID(env, clazz, "resultCode", "I");
}



void SemaphoreResult_setResultCode(JNIEnv *env, jobject this, int code)
{
	(*env)->SetIntField(env, this, resultCodeField, (jint) code);
}


void SemaphoreResult_setErrorCode(JNIEnv *env, jobject this, int code)
{
	(*env)->SetIntField(env, this, errorCodeField, (int) code);
}


void SemaphoreResult_setHandle(JNIEnv *env, jobject this, int id)
{
	jlong value;

	value = (jlong) ((int) id);
	(*env)->SetLongField(env, this, handleField, value);
}
