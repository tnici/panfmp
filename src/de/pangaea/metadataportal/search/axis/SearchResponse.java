/*
 *   Copyright 2007 panFMP Developers Team c/o Uwe Schindler
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package de.pangaea.metadataportal.search.axis;

import de.pangaea.metadataportal.search.SearchResultList;

public final class SearchResponse {

    public SearchResponse(SearchResultList list, int offset, int count, boolean returnXML, boolean returnStoredFields) {
        this.list=list;
        this.offset=offset;
        this.count=count;
        this.returnXML=returnXML;
        this.returnStoredFields=returnStoredFields;
    }

    public int getOffset() {
        return offset;
    }

    public SearchResponseItem[] getResults() throws java.io.IOException {
        if (list.size()-offset<count) count=list.size()-offset;
        if (count<0) count=0;
        SearchResponseItem[] results=new SearchResponseItem[count];
        for (int i=0; i<count; i++) {
            results[i]=new SearchResponseItem(list.getResult(offset+i),returnXML,returnStoredFields);
        }
        return results;
    }

    public long getQueryTime() {
        return list.getQueryTime();
    }

    public int getTotalCount() {
        return list.size();
    }

    // data
    private int offset,count;
    private SearchResultList list;
    private boolean returnXML,returnStoredFields;
}