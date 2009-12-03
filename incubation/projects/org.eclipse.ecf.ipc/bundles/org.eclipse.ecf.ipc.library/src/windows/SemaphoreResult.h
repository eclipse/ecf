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
 * SemaphoreResult.h
 *
 *  Created on: Mar 5, 2009
 *      Author: cnh
 */
#include <windows.h>
#include <jni.h>

#ifndef SEMAPHORERESULT_H_
#define SEMAPHORERESULT_H_

extern void SemaphoreResult_setResultCode(JNIEnv *env, jobject this, int code);
extern void SemaphoreResult_setErrorCode(JNIEnv *env, jobject this, int code);
extern void SemaphoreResult_setHandle(JNIEnv *env, jobject this, int handle);
extern void SemaphoreResult_initialize(JNIEnv *env);

#endif /* SEMAPHORERESULT_H_ */
