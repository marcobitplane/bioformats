/*
 * #%L
 * BSD implementations of Bio-Formats readers and writers
 * %%
 * Copyright (C) 2005 - 2023 Open Microscopy Environment:
 *   - Board of Regents of the University of Wisconsin-Madison
 *   - Glencoe Software, Inc.
 *   - University of Dundee
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package loci.formats.dicom;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import loci.common.Constants;
import loci.common.DataTools;

/**
 * Provide DICOM tags from a file containing the output of dcdump.
 */
public class DCDumpProvider implements ITagProvider {

  private List<DicomTag> tags = new ArrayList<DicomTag>();

  @Override
  public void readTagSource(String location) throws IOException {
    String[] lines = DataTools.readFile(location).split("\r\n");

    // TODO: does not currently handle tag hierarchies
    for (String line : lines) {
      String[] tokens = line.split(" ");
      String[] tag = tokens[0].replaceAll("()", "").split(",");

      String vr = tokens[1];
      vr = vr.substring(vr.indexOf("<") + 1, vr.indexOf(">"));

      String length = null;
      String value = "";
      for (int i=2; i<tokens.length; i++) {
        if (tokens[i].startsWith("VL")) {
          length = tokens[i];
        }
        else if (tokens[i].startsWith("<")) {
          value += tokens[i];
          while (!value.endsWith(">")) {
            i++;
            value += tokens[i];
          }
        }
      }

      int tagUpper = Integer.parseInt(tag[0], 16);
      int tagLower = Integer.parseInt(tag[1], 16);
      int vrCode = DataTools.bytesToShort(vr.getBytes(Constants.ENCODING), false);

      DicomTag t = new DicomTag(DicomAttribute.get(tagUpper << 16 | tagLower), DicomVR.get(vrCode));
      // TODO: fix value handling for more complex cases (e.g. arrays)
      t.value = value;
      tags.add(t);
    }
  }

  @Override
  public List<DicomTag> getTags() {
    return tags;
  }

}
