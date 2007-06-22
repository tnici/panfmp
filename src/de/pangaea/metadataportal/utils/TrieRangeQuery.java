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

package de.pangaea.metadataportal.utils;

import org.apache.lucene.search.*;
import org.apache.lucene.index.*;
import java.io.IOException;
import java.util.BitSet;
import java.util.Date;

public class TrieRangeQuery extends Query {

    private static org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory.getLog(TrieRangeQuery.class);

    // Generic constructor: uses already converted min/max fields
    public TrieRangeQuery(String field, String min, String max) {
        if (min==null && max==null) throw new IllegalArgumentException("The min and max values cannot be both null.");
        this.min=(min==null) ? LuceneConversions.LUCENE_NUMERIC_MIN : min;
        this.max=(max==null) ? LuceneConversions.LUCENE_NUMERIC_MAX : max;
        this.field=field.intern();
    }

    // Constructors for different numeric datatypes
    public TrieRangeQuery(String field, Double min, Double max) {
        if (min==null && max==null) throw new IllegalArgumentException("The min and max double values cannot be both null.");
        this.min=(min==null) ? LuceneConversions.LUCENE_NUMERIC_MIN : LuceneConversions.doubleToLucene(min.doubleValue());
        this.max=(max==null) ? LuceneConversions.LUCENE_NUMERIC_MAX : LuceneConversions.doubleToLucene(max.doubleValue());
        this.field=field.intern();
    }

    public TrieRangeQuery(String field, Date min, Date max) {
        if (min==null && max==null) throw new IllegalArgumentException("The min and max date values cannot be both null.");
        this.min=(min==null) ? LuceneConversions.LUCENE_NUMERIC_MIN : LuceneConversions.dateToLucene(min);
        this.max=(max==null) ? LuceneConversions.LUCENE_NUMERIC_MAX : LuceneConversions.dateToLucene(max);
        this.field=field.intern();
    }

    public TrieRangeQuery(String field, Long min, Long max) {
        if (min==null && max==null) throw new IllegalArgumentException("The min and max long values cannot be both null.");
        this.min=(min==null) ? LuceneConversions.LUCENE_NUMERIC_MIN : LuceneConversions.longToLucene(min.longValue());
        this.max=(max==null) ? LuceneConversions.LUCENE_NUMERIC_MAX : LuceneConversions.longToLucene(max.longValue());
        this.field=field.intern();
    }

    public String toString(String field) {
        StringBuilder sb=new StringBuilder();
        if (!this.field.equals(field)) sb.append(this.field+':');
        sb.append('[');
        sb.append(min);
        sb.append(" TO ");
        sb.append(max);
        sb.append(']');
        return sb.toString();
    }

    public boolean equals(Object o) {
        if (o instanceof TrieRangeQuery) {
            TrieRangeQuery q=(TrieRangeQuery)o;
            return (field==q.field && min.equals(q.min) && max.equals(q.max));
        } else return false;
    }

    public int hashCode() {
        return field.hashCode()+(min.hashCode()^0x14fa55fb)+(max.hashCode()^0x733fa5fe);
    }

    public Query rewrite(IndexReader reader) throws java.io.IOException {
        ConstantScoreQuery q = new ConstantScoreQuery(new TrieRangeFilter(field,min,max));
        q.setBoost(getBoost());
        return q.rewrite(reader);
    }

    // members
    protected String field,min,max;

    protected final class TrieRangeFilter extends Filter {

        private String field;
        private String min;
        private String max;

        protected TrieRangeFilter(String field, String min, String max) {
            this.field = field.intern();
            this.min = min;
            this.max = max;
        }

        // code borrowed from original RangeFilter and simplified (and returns number of terms)
        private int setBits(IndexReader reader, TermDocs termDocs, BitSet bits, String lowerTerm, String upperTerm) throws IOException {
            int count=0,len=lowerTerm.length();
            // add padding before loose/inprecise values to group them
            if (len<16) {
                len++; // length is longer by 1 char because of padding
                lowerTerm=new StringBuilder(len).append((char)(LuceneConversions.LUCENE_LOOSE_PADDING_START+(len/2))).append(lowerTerm).toString();
                upperTerm=new StringBuilder(len).append((char)(LuceneConversions.LUCENE_LOOSE_PADDING_START+(len/2))).append(upperTerm).toString();
            }
            TermEnum enumerator = reader.terms(new Term(field, lowerTerm));
            try {
                do {
                    Term term = enumerator.term();
                    if (term!=null && term.field()==field) {
                        // break out when upperTerm reached or length of term is different
                        String t=term.text();
                        if (len!=t.length() || t.compareTo(upperTerm)>0) break;
                        // we have a good term, find the docs
                        count++;
                        termDocs.seek(enumerator);
                        while (termDocs.next()) bits.set(termDocs.doc());
                    } else break;
                } while (enumerator.next());
            } finally {
                enumerator.close();
            }
            return count;
        }

        // splits range recursively (and returns number of terms)
        private int splitRange(IndexReader reader, TermDocs termDocs, BitSet bits, String min, boolean lowerBoundOpen, String max, boolean upperBoundOpen) throws IOException {
            int length=min.length(),count=0;
            String minShort=lowerBoundOpen ? min.substring(0,length-2) : LuceneConversions.incrementLucene(min.substring(0,length-2));
            String maxShort=upperBoundOpen ? max.substring(0,length-2) : LuceneConversions.decrementLucene(max.substring(0,length-2));

            if (length==2 || minShort.compareTo(maxShort)>=0) {
                count+=setBits(reader,termDocs,bits,min,max);
            } else {
                if (!lowerBoundOpen) count+=setBits(reader,termDocs,bits,min,minShort+LuceneConversions.LUCENE_SYMBOL_MIN+LuceneConversions.LUCENE_SYMBOL_MIN);
                count+=splitRange(reader,termDocs,bits,minShort,lowerBoundOpen,maxShort,upperBoundOpen);
                if (!upperBoundOpen) count+=setBits(reader,termDocs,bits,maxShort+LuceneConversions.LUCENE_SYMBOL_MAX+LuceneConversions.LUCENE_SYMBOL_MAX, max);
            }
            return count;
        }

        /**
         * Returns a BitSet with true for documents which should be
         * permitted in search results, and false for those that should
         * not.
         */
        public BitSet bits(IndexReader reader) throws IOException {
            BitSet bits = new BitSet(reader.maxDoc());
            TermDocs termDocs=reader.termDocs();
            try {
                int count=splitRange(reader,termDocs,bits,min,LuceneConversions.LUCENE_NUMERIC_MIN.equals(min),max,LuceneConversions.LUCENE_NUMERIC_MAX.equals(max));
                if (log.isDebugEnabled()) log.debug("Found "+count+" distinct terms in filtered range for field '"+field+"'.");
            } finally {
                termDocs.close();
            }
            return bits;
        }
    }


}