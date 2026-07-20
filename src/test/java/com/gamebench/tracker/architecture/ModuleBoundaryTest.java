package com.gamebench.tracker.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * 模块边界护栏（声明式，避免对未分层基础类型误报）。
 *
 * 约束：
 * 1. Controller 不直接依赖 Mapper（必须经 Service）。
 * 2. common 包不反向依赖 game 包。
 * 3. game 包内不向上依赖 controller 层。
 */
@AnalyzeClasses(packages = "com.gamebench.tracker", importOptions = ImportOption.DoNotIncludeTests.class)
public class ModuleBoundaryTest {

    @ArchTest
    static final ArchRule controllerMustNotDependOnMapper =
            noClasses().that().resideInAPackage("..game.controller..")
                    .should().dependOnClassesThat().resideInAPackage("..game.mapper..");

    @ArchTest
    static final ArchRule commonMustNotDependOnGame =
            noClasses().that().resideInAPackage("..common..")
                    .should().dependOnClassesThat().resideInAPackage("..game..");

    @ArchTest
    static final ArchRule noUpwardDependencyToController =
            noClasses().that().resideInAPackage("..game..")
                    .should().dependOnClassesThat().resideInAPackage("..game.controller..");
}
