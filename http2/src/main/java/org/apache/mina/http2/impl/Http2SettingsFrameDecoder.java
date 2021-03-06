/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *  
 *    http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License. 
 *  
 */
package org.apache.mina.http2.impl;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.mina.http2.api.Http2Setting;
import org.apache.mina.http2.api.Http2SettingsFrame.Http2SettingsFrameBuilder;
import org.apache.mina.http2.impl.Http2FrameHeadePartialDecoder.Http2FrameHeader;

/**
 * 
 * @author <a href="http://mina.apache.org">Apache MINA Project</a>
 */
public class Http2SettingsFrameDecoder extends Http2FrameDecoder {

    private int remaining;
    
    private LongPartialDecoder decoder = new LongPartialDecoder(6);
    
    private Http2SettingsFrameBuilder builder = new Http2SettingsFrameBuilder();
    
    private Collection<Http2Setting> settings = new ArrayList<Http2Setting>();
    
    public Http2SettingsFrameDecoder(Http2FrameHeader header) {
        super(header);
        remaining = header.getLength() / 6;
        initBuilder(builder);
    }
    
    /* (non-Javadoc)
     * @see org.apache.mina.http2.api.PartialDecoder#consume(java.nio.ByteBuffer)
     */
    @Override
    public boolean consume(ByteBuffer buffer) {
        while ((getValue() == null) && buffer.remaining() > 0) {
            if (decoder.consume(buffer)) {
                remaining--;
                Http2Setting setting = new Http2Setting();
                setting.setID((int) ((decoder.getValue() & 0x00FFFF00000000L) >> 32));
                setting.setValue((decoder.getValue() & 0x00FFFFFFFFL));
                settings.add(setting);
                decoder.reset();
                if (remaining == 0) {
                    builder.settings(settings);
                    setValue(builder.build());
                }
            }
        }
        return getValue() != null;
    }

    /* (non-Javadoc)
     * @see org.apache.mina.http2.api.PartialDecoder#reset()
     */
    @Override
    public void reset() {
    }

}
