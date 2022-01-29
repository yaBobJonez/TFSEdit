package com.yabobjonez.tufairesites;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;

import dev.anarchy.ace.AceEditor;
import dev.anarchy.ace.Modes;
import dev.anarchy.ace.Themes;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Separator;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToolBar;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class Main extends Application { //TODO theming, builder
	public static void main(String[] args) { launch(args); }
	private File baseDir = null;
	private File currFile = null;
	@Override
	public void start(Stage stage) throws Exception {
		TextInputDialog dlgName = new TextInputDialog();
		dlgName.setHeaderText("Введите имя файла с расширением (.html/.css/.js).");
		dlgName.setContentText("Имя файла:");
		dlgName.setOnShowing(e -> dlgName.getEditor().clear());
		TextInputDialog dlgNameFolder = new TextInputDialog();
		dlgNameFolder.setHeaderText("Введите имя папки.");
		dlgNameFolder.setContentText("Имя папки:");
		dlgNameFolder.setOnShowing(e -> dlgNameFolder.getEditor().clear());
		DirectoryChooser dlgDir = new DirectoryChooser();
		dlgDir.setTitle("Выберите папку");
		Alert dlgDel = new Alert(AlertType.CONFIRMATION, "Вы действительно хотите удалить выбранное?", ButtonType.YES, ButtonType.NO);
		dlgDel.setHeaderText("Удаление.");
		TreeView<String> explorer = new TreeView<>();
		WebView view = new WebView();
		AceEditor editor = new AceEditor();
		editor.setTheme(Themes.Eclipse);
		Button btnOpen = new Button(); btnOpen.setGraphic(getIcon("open.png")); btnOpen.setOnAction(e -> {
			this.baseDir = dlgDir.showDialog(stage);
			if(this.baseDir!=null) explorer.setRoot(this.intlBuildTree(this.baseDir));
		});
		Button btnRefresh = new Button(); btnRefresh.setGraphic(getIcon("refresh.png")); btnRefresh.setOnAction(e -> {
			if(this.baseDir!=null) explorer.setRoot(this.intlBuildTree(this.baseDir));
		});
		Button btnSave = new Button(); btnSave.setGraphic(getIcon("save.png"));btnSave.setOnAction(e -> {
			if(this.baseDir!=null && this.currFile!=null) try {
				Files.writeString(this.currFile.toPath(), editor.getText(),
					Charset.forName("UTF-8"), StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
			} catch(IOException exc){ System.err.println("Ошибка R/W!"); }
		});
		Button btnDelete = new Button(); btnDelete.setGraphic(getIcon("delete.png")); btnDelete.setOnAction(e -> { if(this.baseDir!=null){
			if(!explorer.getSelectionModel().isEmpty() && explorer.getSelectionModel().getSelectedItem().getValue()
			.equals(this.baseDir.getName())){
				String file = "";
				for(TreeItem<String> selected = explorer.getSelectionModel().getSelectedItem();
					selected.getParent() != null; selected = selected.getParent()) file = File.separator + selected.getValue() + file;
				try { if(dlgDel.showAndWait().get() == ButtonType.YES){
					Files.walk(new File(this.baseDir, file).toPath()).sorted(Comparator.reverseOrder())
						.map(Path::toFile).forEach(File::delete);
					explorer.setRoot(this.intlBuildTree(this.baseDir));
				}} catch(IOException exc) { System.err.println("Ошибка DW!"); }
			}
		}});
		Button btnNewF = new Button(); btnNewF.setGraphic(getIcon("newfolder.png")); btnNewF.setOnAction(e -> { if(this.baseDir!=null){
			if(explorer.getSelectionModel().isEmpty()) return;
			String file = "";
			for(TreeItem<String> selected = explorer.getSelectionModel().getSelectedItem();
				selected.getParent() != null; selected = selected.getParent()) file = File.separator + selected.getValue() + file;
			File f = new File(this.baseDir, file); if(f.isFile()) return;
			dlgNameFolder.showAndWait().ifPresent(name -> {
				if(!new File(f, name).mkdirs()){ System.err.println("Ошибка DirR/W!"); }
				explorer.setRoot(this.intlBuildTree(this.baseDir));
			});
		}});
		Button btnNewf = new Button(); btnNewf.setGraphic(getIcon("newfile.png")); btnNewf.setOnAction(e -> { if(this.baseDir!=null){
			if(explorer.getSelectionModel().isEmpty()) return;
			String file = "";
			for(TreeItem<String> selected = explorer.getSelectionModel().getSelectedItem();
				selected.getParent() != null; selected = selected.getParent()) file = File.separator + selected.getValue() + file;
			File f2 = new File(this.baseDir, file); if(f2.isFile()) return;
			dlgName.showAndWait().ifPresent(name -> { try {
				File f = new File(f2, name); f.createNewFile();
				if(f.getName().endsWith(".html")){ Files.writeString(f.toPath(),
					"<!DOCTYPE html>\n<html>\n\t<head>\n\t\t<meta charset=\"utf-8\">\n\t\t<title></title>\n\t</head>\n\t<body>\n"
					+"\t\t\n\t</body>\n</html>",
					Charset.forName("UTF-8"), StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
					editor.setMode(Modes.HTML); }
				else if(f.getName().endsWith(".css")) editor.setMode(Modes.CSS);
				else if(f.getName().endsWith(".js")) editor.setMode(Modes.JavaScript);
				editor.setText(Files.readString(f.toPath()));
				explorer.setRoot(this.intlBuildTree(this.baseDir));
			} catch(IOException exc){ System.err.println("Ошибка R/W!"); } });
		}}); Button btnRun = new Button(); btnRun.setGraphic(getIcon("run.png")); btnRun.setOnAction(e -> {
			if(this.baseDir!=null && this.currFile!=null) view.getEngine().load(this.currFile.toURI().toString());
		}); Alert dlgAbout = new Alert(AlertType.INFORMATION, "", ButtonType.CLOSE);
		dlgAbout.setTitle("О программе");
		dlgAbout.setHeaderText("TuFaireSites Editor");
		dlgAbout.setContentText("Версия: 1.1.0-rc\nАвтор: Михайло Стецюк (ya_Bob_Jonez)\nЛицензия: НЕ ДЛЯ РАСПРОСТРАНЕНИЯ\n\n"
			+"Использовано:\nOpenJDK 11.0 [GPLv2+CE]\nOpenJFX 16EA [GPLv2+CE]\nAceFX (orange451) [Apache2.0]\nhttps://icons8.com");
		Button btnAbout = new Button(); btnAbout.setGraphic(getIcon("about.png")); btnAbout.setOnAction(e -> dlgAbout.show());
		Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
		ToolBar toolbar = new ToolBar(
			btnOpen, btnRefresh, new Separator(Orientation.VERTICAL),
			btnNewF, btnNewf, btnDelete, btnSave, new Separator(Orientation.VERTICAL),
			btnRun, spacer,
			btnAbout
		); explorer.setOnMouseClicked(e -> {
			if(e.getClickCount()==2){
				if(explorer.getSelectionModel().isEmpty()) return;
				String file = "";
				for(TreeItem<String> selected = explorer.getSelectionModel().getSelectedItem();
					selected.getParent() != null; selected = selected.getParent()) file = File.separator + selected.getValue() + file;
				File f = new File(this.baseDir, file);
				if(f.isFile()){ try {
					if(this.currFile!=null) Files.writeString(this.currFile.toPath(), editor.getText(),
						Charset.forName("UTF-8"), StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
					this.currFile = f;
					if(currFile.getName().endsWith(".html")) editor.setMode(Modes.HTML);
					else if(currFile.getName().endsWith(".css")) editor.setMode(Modes.CSS);
					else if(currFile.getName().endsWith(".js")) editor.setMode(Modes.JavaScript);
					editor.setText(Files.readString(f.toPath()));
					} catch(IOException exc){ System.err.println("Ошибка R/W!"); }
				} else if(f.isDirectory() && !explorer.getSelectionModel().getSelectedItem().getChildren().isEmpty())
					explorer.getSelectionModel().getSelectedItem().setExpanded(!explorer.getSelectionModel().getSelectedItem().isExpanded());
			} else if(e.getButton() == MouseButton.SECONDARY) explorer.getSelectionModel().clearSelection();
		});
		SplitPane main = new SplitPane(explorer, view, editor);
		main.setDividerPositions(0.2, 0.6);
		VBox.setVgrow(main, Priority.ALWAYS);
		VBox root = new VBox(toolbar, main);
		Scene scene = new Scene(root, 640, 480);
		scene.setOnKeyPressed(e -> {
			if(e.getCode() == KeyCode.F5) btnRun.fireEvent(new ActionEvent());
			else if(e.getCode() == KeyCode.F6){ btnSave.fireEvent(new ActionEvent()); btnRun.fireEvent(new ActionEvent()); }
		}); stage.setTitle("TFSEdit by ya_Bob_Jonez");
		stage.setScene(scene);
		stage.show();
	} private TreeItem<String> intlBuildTree(File dir){
		TreeItem<String> treeItem = new TreeItem<>(dir.getName());
		for(File f : dir.listFiles()){
			if(f.isDirectory()) treeItem.getChildren().add(this.intlBuildTree(f));
			else treeItem.getChildren().add(new TreeItem<String>(f.getName()));
		} return treeItem;
	} private ImageView getIcon(String rel){ ImageView t = new ImageView(getClass().getResource("img/"+rel).toExternalForm());
		t.setFitWidth(30); t.setPreserveRatio(true); return t; }
}
