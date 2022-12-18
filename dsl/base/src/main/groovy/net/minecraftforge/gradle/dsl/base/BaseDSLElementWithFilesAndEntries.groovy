package net.minecraftforge.gradle.dsl.base


import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.provider.ListProperty;

/**
 * Represents part of an extension which combines a set of files with entries as well as raw addable entries.
 *
 * @param <TSelf> The type of the implementing class.
 */
interface BaseDSLElementWithFilesAndEntries<TSelf extends BaseDSLElementWithFilesAndEntries<TSelf>> extends ProjectAssociatedBaseDSLElement<TSelf> {

    /**
     * @returns The files which contain entries relevant to this extension.
     */
    ConfigurableFileCollection getFiles();

    /**
     * @returns The raw additional entries relevant to this extension.
     */
    ListProperty<String> getEntries();

    /**
     * Indicates if either at least one file is specified or at least one additional raw entry is specified.
     *
     * @return {@code true}, when at least one file or entry is specified. False otherwise.
     */
    boolean isEmpty();
}
