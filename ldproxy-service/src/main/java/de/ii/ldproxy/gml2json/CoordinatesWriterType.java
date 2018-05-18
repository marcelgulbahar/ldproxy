/**
 * Copyright 2018 interactive instruments GmbH
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package de.ii.ldproxy.gml2json;

import de.ii.xtraplatform.crs.api.CrsTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Writer;

/**
 *
 * @author zahnen
 */
public enum CoordinatesWriterType {

    DEFAULT {
        @Override
        Writer create(Builder builder) {
            //LOGGER.debug("creating GML2JsonCoordinatesWriter");
            return new DefaultCoordinatesWriter(builder.formatter, builder.srsDimension);
        }
    },
    SWAP {
        @Override
        Writer create(Builder builder) {
            //LOGGER.debug("creating GML2JsonFastXYSwapCoordinatesWriter");
            return new FastXYSwapCoordinatesWriter(builder.formatter, builder.srsDimension);
        }
    },
    TRANSFORM {
        @Override
        Writer create(Builder builder) {
            //LOGGER.debug("creating GML2JsonTransCoordinatesWriter");
            return new TransformingCoordinatesWriter(builder.formatter, builder.srsDimension, builder.transformer);
        }
    },
    BUFFER_TRANSFORM {
        @Override
        Writer create(Builder builder) {
            //LOGGER.debug("creating GML2JsonBufferedTransformingCoordinatesWriter");
            return new BufferedTransformingCoordinatesWriter(builder.formatter, builder.srsDimension, builder.transformer, builder.swap, builder.reversepolygon);
        }
    }/*,
    SIMPLIFY_BUFFER_TRANSFORM {
        @Override
        Writer create(Builder builder) {
            //LOGGER.debug("creating GML2JsonSimplifiyingBufferedTransformingCoordinatesWriter");
            return new SimplifiyingBufferedTransformingCoordinatesWriter(builder.jsonOut, builder.srsDimension, builder.transformer, builder.simplifier, builder.swap, builder.reversepolygon);
        }
    }*/;

    abstract Writer create(Builder builder);

    private static final Logger LOGGER = LoggerFactory.getLogger(CoordinatesWriterType.class);
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static Builder builder(CoordinateFormatter formatter) {
        return new Builder().format(formatter);
    }
    
    public static class Builder {
        private CoordinateFormatter formatter;
        private int srsDimension;
        private boolean swap;
        private boolean transform;
        private boolean simplify;
        private boolean reversepolygon;
        private CrsTransformer transformer;
        //private DouglasPeuckerLineSimplifier simplifier;
        
        public Builder () {
            this.srsDimension = 2;
            this.swap = false;
            this.transform = false;
            this.simplify = false;
            this.reversepolygon = false;
        }
        
        public CoordinatesWriterType getType() {
            /*if (simplify) {
                return CoordinatesWriterType.SIMPLIFY_BUFFER_TRANSFORM;
            }
            else*/ if (reversepolygon) {
                return CoordinatesWriterType.BUFFER_TRANSFORM;
            }
            else if (transform) {
                return CoordinatesWriterType.TRANSFORM;
            }
            else if (swap) {
                return CoordinatesWriterType.SWAP;
            }
            return CoordinatesWriterType.DEFAULT;
        }
        
        public Writer build() {
            if (formatter == null) {
                return null;
            }
            return getType().create(this);
        }
        
        public Builder format(CoordinateFormatter formatter) {
            this.formatter = formatter;
            return this;
        }
        
        public Builder dimension(int dim) {
            this.srsDimension = dim;
            return this;
        }
        
        public Builder swap() {
            this.swap = true;
            return this;
        }
        
        public Builder reversepolygon() {
            this.reversepolygon = true;
            return this;
        }
        
        public Builder transformer(CrsTransformer transformer) {
            this.transformer = transformer;
            this.transform = true;
            return this;
        }
        
        /*public Builder simplifier(DouglasPeuckerLineSimplifier simplifier) {
            this.simplifier = simplifier;
            this.simplify = true;
            return this;
        }*/
    }
}