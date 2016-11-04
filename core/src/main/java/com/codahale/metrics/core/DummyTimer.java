/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

package com.codahale.metrics.core;

import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * Performs no action. User: jeichar Date: 4/3/12 Time: 12:02 PM
 */
public class DummyTimer extends Timer {
    public static final DummyTimer INSTANCE = new DummyTimer();

    DummyTimer() {
        super();
    }

    @Override
    public Snapshot getSnapshot() {
        return null;
    }


    @Override
    public Context time() {
        return super.time();    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public <T> T time(Callable<T> event) throws Exception {
        return super.time(event);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void update(long duration, TimeUnit unit) {
        super.update(duration, unit);    //To change body of overridden methods use File | Settings | File Templates.
    }
}
