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
#include <sys/types.h>
#include <sys/ipc.h>
#include <jni.h>

//
// The neighbor of the beast
//
#define PROJID 668

/**
 * Convert a Java supplied string to a key_t that is suitable for use in identifying
 * a semaphore.
 *
 * The function uses the constant PROJID in the ftok function to come up with the 
 * identifying value.
 */
key_t jstringToKey(JNIEnv *env, jstring s)
{
	char name[512];
	int length;
	key_t key;


	length = (*env)->GetStringLength(env, s);
	(*env)->GetStringUTFRegion(env, s, 0, length, name);
	key = ftok(name, PROJID);

	return key;
}



