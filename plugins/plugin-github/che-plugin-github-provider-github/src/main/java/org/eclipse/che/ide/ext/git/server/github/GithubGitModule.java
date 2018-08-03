/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.ext.git.server.github;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import org.eclipse.che.api.git.CredentialsProvider;
import org.eclipse.che.inject.DynaModule;

/**
 * The module that contains configuration of the server side part of the Git extension.
 *
 * @author Alexander Garagatyi
 */
@DynaModule
public class GithubGitModule extends AbstractModule {

  /** {@inheritDoc} */
  @Override
  protected void configure() {
    Multibinder.newSetBinder(binder(), CredentialsProvider.class)
        .addBinding()
        .to(GitHubOAuthCredentialProvider.class);
  }
}
