package dev.freya02.botcommands.jda.ktx.deprecation.utils;

import com.google.devtools.ksp.impl.symbol.kotlin.KSFunctionDeclarationImpl;
import com.google.devtools.ksp.symbol.KSFunctionDeclaration;
import ksp.org.jetbrains.kotlin.analysis.api.symbols.KaFunctionSymbol;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class KaAccessor {

    public static KaFunctionSymbol getKaFunctionSymbol(KSFunctionDeclaration declaration) {
        return ((KSFunctionDeclarationImpl) declaration).getKtFunctionSymbol$kotlin_analysis_api();
    }
}
