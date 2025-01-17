/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.trino.parquet.reader.decoders;

import io.airlift.slice.Slices;
import io.trino.parquet.reader.SimpleSliceInputStream;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Random;

import static io.trino.parquet.reader.decoders.IntBitUnpackers.getIntBitUnpacker;
import static org.assertj.core.api.Assertions.assertThat;

public class TestBitUnpackers
{
    @Test(dataProvider = "length")
    public void testIntUnpackersUnpack(int length)
    {
        for (int i = 0; i < 500; i++) {
            long seed = (long) i * length;
            Random random = new Random(seed);
            for (int bitWidth = 0; bitWidth <= 32; bitWidth++) {
                ApacheParquetIntUnpacker parquetUnpacker = new ApacheParquetIntUnpacker(bitWidth);
                IntBitUnpacker optimizedUnpacker = getIntBitUnpacker(bitWidth);

                byte[] buffer = new byte[(bitWidth * length) / Byte.SIZE + 1];
                random.nextBytes(buffer);

                int[] parquetUnpackerOutput = new int[length];
                int[] optimizedUnpackerOutput = new int[length];
                parquetUnpacker.unpack(parquetUnpackerOutput, 0, asSliceStream(buffer), length);
                optimizedUnpacker.unpack(optimizedUnpackerOutput, 0, asSliceStream(buffer), length);

                assertThat(optimizedUnpackerOutput)
                        .as("Error at bit width %d, random seed %d", bitWidth, seed)
                        .isEqualTo(parquetUnpackerOutput);
            }
        }
    }

    @DataProvider(name = "length")
    public static Object[][] length()
    {
        return new Object[][] {{24}, {72}, {168}, {304}, {376}, {8192}};
    }

    private SimpleSliceInputStream asSliceStream(byte[] buffer)
    {
        return new SimpleSliceInputStream(Slices.wrappedBuffer(buffer));
    }
}
