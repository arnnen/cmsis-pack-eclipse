/**
 * generated by Xtext 2.10.0
 */
package com.arm.cmsis.pack.debugseq;

import com.arm.cmsis.pack.debugseq.AbstractDebugSeqRuntimeModule;
import com.arm.cmsis.pack.debugseq.converter.DebugSeqTerminalConverters;
import com.arm.cmsis.pack.debugseq.lexer.DebugSeqLexer;
import com.google.inject.Binder;
import com.google.inject.name.Names;
import org.eclipse.xtext.conversion.IValueConverterService;
import org.eclipse.xtext.parser.antlr.Lexer;
import org.eclipse.xtext.parser.antlr.LexerBindings;

/**
 * Use this class to register components to be used at runtime / without the Equinox extension registry.
 */
@SuppressWarnings("all")
public class DebugSeqRuntimeModule extends AbstractDebugSeqRuntimeModule {
  @Override
  public Class<? extends IValueConverterService> bindIValueConverterService() {
    return DebugSeqTerminalConverters.class;
  }
  
  @Override
  public void configureRuntimeLexer(final Binder binder) {
    binder.<Lexer>bind(Lexer.class).annotatedWith(Names.named(LexerBindings.RUNTIME)).to(DebugSeqLexer.class);
  }
}
