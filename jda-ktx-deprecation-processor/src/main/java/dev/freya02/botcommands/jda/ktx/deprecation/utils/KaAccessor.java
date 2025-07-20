package dev.freya02.botcommands.jda.ktx.deprecation.utils;

import com.google.devtools.ksp.impl.symbol.kotlin.KSFunctionDeclarationImpl;
import com.google.devtools.ksp.symbol.KSFunctionDeclaration;
import ksp.org.jetbrains.kotlin.analysis.api.symbols.KaFunctionSymbol;
import org.jetbrains.annotations.NotNull;

public class KaAccessor {

    @NotNull
    public static KaFunctionSymbol getKaFunctionSymbol(@NotNull KSFunctionDeclaration declaration) {
        return ((KSFunctionDeclarationImpl) declaration).getKtFunctionSymbol$kotlin_analysis_api();
    }
}
