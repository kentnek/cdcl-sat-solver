package com.kentnek.cdcl.algo.preprocessor;

import com.kentnek.cdcl.model.Assignment;
import com.kentnek.cdcl.model.Formula;

/**
 * Performs initial processing on the formula.
 * <p>
 *
 * @author kentnek
 */

public interface FormulaPreprocessor {
    Formula preprocess(Formula formula, Assignment assignment);
}
