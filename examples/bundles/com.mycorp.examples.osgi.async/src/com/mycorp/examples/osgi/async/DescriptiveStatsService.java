/*******************************************************************************
 * Copyright (c) 2018 Composent, Inc. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Scott Lewis - initial API and implementation
 ******************************************************************************/
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
