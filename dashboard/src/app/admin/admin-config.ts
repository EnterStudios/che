/*
 * Copyright (c) 2015-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
'use strict';

import {AdminsUserManagementConfig} from './user-management/user-management-config';

/**
 * @author Florent Benoit
 */
export class AdminsConfig {

  constructor(register: che.IRegisterService) {
    /* tslint:disable */
    new AdminsUserManagementConfig(register);
    /* tslint:enable */
  }
}

