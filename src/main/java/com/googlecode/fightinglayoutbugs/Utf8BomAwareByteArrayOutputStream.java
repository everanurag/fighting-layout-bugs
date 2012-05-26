/*
 * Copyright 2009-2012 Michael Tamm
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.googlecode.fightinglayoutbugs;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

/**
 * @author Michael Tamm
 */
class Utf8BomAwareByteArrayOutputStream extends ByteArrayOutputStream {

    public boolean hasUtf8Bom() {
        return (count >= 3 && buf[0] == ((byte) 0xEF) && buf[1] == ((byte) 0xBB) && buf[2] == ((byte) 0xBF));
    }

    @Override
    public String toString(String charsetName) throws UnsupportedEncodingException {
        if (hasUtf8Bom() && "UTF-8".equals(charsetName)) {
            return new String(buf, 3, count - 3, "UTF-8");
        } else {
            return super.toString(charsetName);
        }
    }

    @Override
    public String toString() {
        if (hasUtf8Bom()) {
            try {
                return new String(buf, 3, count - 3, "UTF-8");
            } catch(UnsupportedEncodingException e) {
                // Should never happen.
                return super.toString();
            }
        } else {
            return super.toString();
        }
    }
}
