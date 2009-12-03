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
 * IPCPackage.c
 *
 *  Created on: Mar 5, 2009
 *      Author: cnh
 */

#include <jni.h>
#include <windows.h>
#include <stdio.h>
#include "IPCPackage.h"
#include "SemaphoreResult.h"
#include "FIFOImpl.h"
#include "FIFOResult.h"

static int initialized = 0;

JNIEXPORT void JNICALL
Java_org_eclipse_ecf_ipc_IPCPackage_initializeNative(JNIEnv *env, jclass thisClass)
{
	if (initialized)
		return;

	SemaphoreResult_initialize(env);
	FIFOImpl_initialize(env);
	FIFOResult_initialize(env);

	initialized = 1;
}


JNIEXPORT jobject JNICALL
Java_org_eclipse_ecf_ipc_IPCPackage_createBuffer(JNIEnv *env, jclass clazz, jint size)
{
	void *buff;
	jobject bb;

	buff = malloc(size);
	bb = (*env)->NewDirectByteBuffer(env, buff, size);
	return bb;
}

