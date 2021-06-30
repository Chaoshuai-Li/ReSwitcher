package switchrefactor.datalog;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;

public class DataLogList {
	private static List<AddressRecord> listBreakRecords = new ArrayList<AddressRecord>();
	private static List<AddressRecord> listDefaultRecords = new ArrayList<AddressRecord>();
	private static List<AddressRecord> listBranchRecords = new ArrayList<AddressRecord>();
	private static List<AddressRecord> listCaseDefaultRecords = new ArrayList<AddressRecord>();
	private static List<DataRecord> listDataRecords = new ArrayList<DataRecord>();

	// ÐÅÏ¢¼ÇÂ¼
	public static void RecordFactory(AddressRecord ar, int label) {
		switch (label) {
		case 1:
			listBreakRecords.add(ar);
			break;
		case 2:
			listDefaultRecords.add(ar);
			break;
		case 3:
			listBranchRecords.add(ar);
			break;
		case 4:
			listCaseDefaultRecords.add(ar);
			break;
		}
	}

	public static void listClear() {
		listBreakRecords.clear();
		listDefaultRecords.clear();
		listBranchRecords.clear();
		listCaseDefaultRecords.clear();
//		listDataRecords.clear();
	}

	public static void DataFactory(DataRecord dr) {
		listDataRecords.add(dr);
	}

	public static void listBreakRecordReback() {
		listBreakRecords.remove(listBreakRecords.size() - 1);
	}

	public static List<AddressRecord> getBreakInformation() {
		return listBreakRecords;
	}

	public static List<AddressRecord> getDefaultInformation() {
		return listDefaultRecords;
	}

	public static List<AddressRecord> getBranchInformation() {
		return listBranchRecords;
	}

	public static List<AddressRecord> getCaseDefaultInformation() {
		return listCaseDefaultRecords;
	}

	public static List<DataRecord> getDataRecords() {
		return listDataRecords;
	}

	public static void removeList(DataRecord dr) {
		if (dr != null) {
			if (!listDataRecords.remove(dr)) {
				MessageDialog.openError(null, "Error", dr.getName() + " failed to delete.");
			}
		}
	}

	public static void removeListDataRecordAll() {
		listDataRecords.clear();
	}
}
