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
 * Accessor methods for the SemaphoreResult class.
 *
 *  Created on: Mar 5, 2009
 *      Author: cnh
 */

#include <jni.h>
#include <errno.h>

#include "SemaphoreResult.h"

static jfieldID handleField;
static jfieldID errorCodeField;
static jfieldID resultCodeField;

#define CLASS_NAME "org/eclipse/ecf/ipc/semaphore/SemaphoreResult"
#define RESULT_SIGNATURE "Lorg/eclipse/ecf/ipc/semaphore/SemaphoreResult$Results;"

void SemaphoreResult_initialize(JNIEnv *env)
{
	jclass clazz;

	clazz = (*env)->FindClass(env, CLASS_NAME);

	handleField = (*env)->GetFieldID(env, clazz, "handle", "J");
	resultCodeField = (*env)->GetFieldID(env, clazz, "resultCode", "I");
	errorCodeField = (*env)->GetFieldID(env, clazz, "errorCode", "I");
}


void SemaphoreResult_setResult(JNIEnv *env, jobject this, int rawCode)
{
	(*env)->SetIntField(env, this, errorCodeField, rawCode);
}


void SemaphoreResult_setHandle(JNIEnv *env, jobject this, int semid)
{
	jlong value;

	value = (jlong) ((int) semid);
	(*env)->SetLongField(env, this, handleField, value);
}

