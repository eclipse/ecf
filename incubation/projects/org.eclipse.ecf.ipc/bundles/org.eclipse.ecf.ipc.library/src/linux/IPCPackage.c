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

#include "IPCPackage.h"
#include "SemaphoreResult.h"
#include "FIFOResult.h"
#include "FIFOImpl.h"

extern void NamedPipeImpl_initialize(JNIEnv *env);


static int initialized = 0;

JNIEXPORT void JNICALL
Java_org_eclipse_ecf_ipc_IPCPackage_initializeNative(JNIEnv *env, jclass thisClass)
{
	if (initialized)
		return;

	FIFOImpl_initialize(env);
	FIFOResult_initialize(env);
	SemaphoreResult_initialize(env);

	initialized = 1;
}
