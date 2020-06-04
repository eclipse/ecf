/****************************************************************************
 * Copyright (c) 2018 Composent, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors: Scott Lewis - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package com.mycorp.examples.osgi.async;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import org.osgi.util.promise.Promise;

public interface DescriptiveStatsService {

	CompletableFuture<Double> mean(Collection<Float> data);
	CompletionStage<Long> sum(Long...data);
	Promise<Double> sumsq(Collection<Double> data);
}
