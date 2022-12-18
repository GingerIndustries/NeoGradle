/*
 * ForgeGradle
 * Copyright (C) 2018 Forge Development LLC
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 * USA
 */

package net.minecraftforge.gradle.common.extensions;

import groovy.lang.GroovyObjectSupport;
import net.minecraftforge.gradle.common.extensions.base.BaseFilesWithEntriesExtension;
import net.minecraftforge.gradle.common.util.IConfigurableObject;
import net.minecraftforge.gradle.common.runtime.naming.NamingChannelProvider;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;

import javax.inject.Inject;

public abstract class MinecraftExtension extends GroovyObjectSupport implements IConfigurableObject<MinecraftExtension> {

    private final Project project;
    private final BaseFilesWithEntriesExtension accessTransformers;
    private final NamedDomainObjectContainer<NamingChannelProvider> namingChannelProviders;

    @Inject
    public MinecraftExtension(final Project project) {
        this.project = project;
        this.accessTransformers = project.getObjects().newInstance(BaseFilesWithEntriesExtension.class, project);
        this.namingChannelProviders = project.getObjects().domainObjectContainer(NamingChannelProvider.class, name -> project.getObjects().newInstance(NamingChannelProvider.class, project, name));
    }

    public Project getProject() {
        return project;
    }

    public NamedDomainObjectContainer<NamingChannelProvider> getNamingChannelProviders() {
        return namingChannelProviders;
    }

    public MappingsExtension getMappings() {
        return project.getExtensions().getByType(MappingsExtension.class);
    }

    public BaseFilesWithEntriesExtension getAccessTransformers() {
        return this.accessTransformers;
    }
}