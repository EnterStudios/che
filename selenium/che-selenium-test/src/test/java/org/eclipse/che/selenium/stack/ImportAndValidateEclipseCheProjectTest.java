/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.stack;

import static org.eclipse.che.commons.lang.NameGenerator.generate;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Workspace.IMPORT_PROJECT;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Workspace.WORKSPACE;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.MarkerLocator.ERROR;
import static org.eclipse.che.selenium.pageobject.Wizard.TypeProject.MAVEN;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.ECLIPSE_CHE;
import static org.openqa.selenium.Keys.BACK_SPACE;
import static org.openqa.selenium.Keys.ESCAPE;

import com.google.inject.Inject;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.ImportProjectFromLocation;
import org.eclipse.che.selenium.pageobject.InformationDialog;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.MavenPluginStatusBar;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.Wizard;
import org.eclipse.che.selenium.pageobject.dashboard.CreateWorkspaceHelper;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Aleksandr Shmaraev */
public class ImportAndValidateEclipseCheProjectTest {

  private static final String WORKSPACE_NAME = generate("EclipseCheWs", 4);
  private static final String PROJECT_NAME = "eclipse-che";
  private static final String ECLIPSE_CHE_PROJECT_URL = "https://github.com/eclipse/che.git";
  private static final String PATH_TO_JAVA_FILE =
      "selenium/che-selenium-test/src/main/java/org/eclipse/che/selenium/pageobject/CodenvyEditor.java";
  private static final String PATH_TO_POM_FILE = "dashboard/pom.xml";
  private static final String PATH_TO_TS_FILE = "dashboard/src/app/index.module.ts";

  @Inject private Ide ide;
  @Inject private Menu menu;
  @Inject private ImportProjectFromLocation importProject;
  @Inject private Loader loader;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Wizard projectWizard;
  @Inject private MavenPluginStatusBar mavenPluginStatusBar;
  @Inject private InformationDialog informationDialog;
  @Inject private CodenvyEditor editor;
  @Inject private Dashboard dashboard;
  @Inject private CreateWorkspaceHelper createWorkspaceHelper;
  @Inject private TestWorkspaceServiceClient workspaceServiceClient;
  @Inject private DefaultTestUser defaultTestUser;

  @BeforeClass
  public void prepare() {
    dashboard.open();
    createWorkspaceHelper.createWorkspaceFromStackWithoutProject(ECLIPSE_CHE, WORKSPACE_NAME);

    ide.switchToIdeAndWaitWorkspaceIsReadyToUse();
    ide.waitOpenedWorkspaceIsReadyToUse();
  }

  @AfterClass
  public void tearDown() throws Exception {
    workspaceServiceClient.delete(WORKSPACE_NAME, defaultTestUser.getName());
  }

  @Test
  public void checkImportAndResolveDependenciesEclipceCheProject() {
    // import the eclipse-che project
    projectExplorer.waitProjectExplorer();
    menu.runCommand(WORKSPACE, IMPORT_PROJECT);
    importProject.waitAndTypeImporterAsGitInfo(ECLIPSE_CHE_PROJECT_URL, PROJECT_NAME);

    // config the project
    projectWizard.waitCreateProjectWizardForm();
    projectWizard.selectTypeProject(MAVEN);
    projectWizard.clickSaveButton();
    loader.waitOnClosed();

    // TODO it is the workaround, delete it after resolve the issue
    // TODO https://github.com/eclipse/che/issues/10515
    // close unexpected error information dialog and click on the 'Save' button;
    int counterErrorDialog = 0;

    while (informationDialog.waitFormIsOpened()) {
      informationDialog.waitFormToOpen();
      informationDialog.clickOkBtn();
      projectWizard.clickSaveButton();
      counterErrorDialog++;
      loader.waitOnClosed();
    }

    System.out.println(
        "==== The error information dialog appeared " + counterErrorDialog + " times ====");

    projectWizard.waitCreateProjectWizardFormIsClosed();
    loader.waitOnClosed();

    // expand the project
    projectExplorer.waitItem(PROJECT_NAME);
    projectExplorer.openItemByPath(PROJECT_NAME);
    loader.waitOnClosed();

    // then open files
    // open a java file
    projectExplorer.quickRevealToItemWithJavaScript(
        String.format("%s/%s", PROJECT_NAME, PATH_TO_JAVA_FILE));
    projectExplorer.openItemByPath(String.format("%s/%s", PROJECT_NAME, PATH_TO_JAVA_FILE));
    editor.waitActive();

    // open a xml file
    projectExplorer.quickRevealToItemWithJavaScript(
        String.format("%s/%s", PROJECT_NAME, PATH_TO_POM_FILE));
    projectExplorer.openItemByPath(String.format("%s/%s", PROJECT_NAME, PATH_TO_POM_FILE));
    editor.waitActive();

    // open a ts file
    projectExplorer.quickRevealToItemWithJavaScript(
        String.format("%s/%s", PROJECT_NAME, PATH_TO_TS_FILE));
    projectExplorer.openItemByPath(String.format("%s/%s", PROJECT_NAME, PATH_TO_TS_FILE));
    editor.waitActive();

    // open the resolving dependencies form
    loader.waitOnClosed();
    mavenPluginStatusBar.waitExpectedTextInInfoPanel("Resolving project:", 120);
    mavenPluginStatusBar.clickOnInfoPanel();

    // should close the resolve dependencies form by Esc
    mavenPluginStatusBar.waitResolveDependenciesFormToOpen();
    mavenPluginStatusBar.closeResolveDependenciesFormByKeys(ESCAPE.toString());

    // then open it again
    mavenPluginStatusBar.clickOnInfoPanel();
    mavenPluginStatusBar.waitResolveDependenciesFormToOpen();

    // wait while dependencies are resolved
    mavenPluginStatusBar.waitClosingInfoPanel(4200);
    mavenPluginStatusBar.waitResolveDependenciesFormToClose();
  }

  @Test(priority = 1)
  public void checkErrorMarkersInEditor() {
    // check an error marker in the ts file
    projectExplorer.openItemByPath(PATH_TO_TS_FILE);
    editor.waitActive();
    editor.typeTextIntoEditor("q");
    editor.waitMarkerInPosition(ERROR, 1);
    editor.typeTextIntoEditor(BACK_SPACE.toString());
    editor.waitMarkerInvisibility(ERROR, 1);

    // check an error marker in the the pom.xml file
    projectExplorer.openItemByPath(PATH_TO_POM_FILE);
    editor.waitActive();
    editor.waitAllMarkersInvisibility(ERROR);
    editor.typeTextIntoEditor("q");
    editor.waitMarkerInPosition(ERROR, 1);
    editor.typeTextIntoEditor(BACK_SPACE.toString());
    editor.waitMarkerInvisibility(ERROR, 1);

    // check an error marker in the java file
    projectExplorer.openItemByPath(PATH_TO_JAVA_FILE);
    editor.waitActive();
    editor.waitAllMarkersInvisibility(ERROR);
    editor.setCursorToLine(12);
    editor.typeTextIntoEditor("q");
    editor.waitMarkerInPosition(ERROR, 12);
    editor.typeTextIntoEditor(BACK_SPACE.toString());
    editor.waitMarkerInvisibility(ERROR, 12);
  }
}
