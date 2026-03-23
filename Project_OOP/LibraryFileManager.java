package Project_OOP;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LibraryFileManager {
    public static final String DEFAULT_MEMBERS_FILE = "src/Project_OOP/library_members.csv";
    public static final String DEFAULT_ITEMS_FILE = "src/Project_OOP/library_items.csv";

    private final Path membersPath;
    private final Path itemsPath;

    public LibraryFileManager() {
        this(DEFAULT_MEMBERS_FILE, DEFAULT_ITEMS_FILE);
    }

    public LibraryFileManager(String membersFile, String itemsFile) {
        this.membersPath = Paths.get(membersFile);
        this.itemsPath = Paths.get(itemsFile);
    }

    public boolean dataFilesExist() {
        return Files.exists(membersPath) && Files.exists(itemsPath);
    }

    public void saveLibraryData(LibrarySystem system) throws IOException {
        saveMembers(system.getAllMembers());
        saveItems(system.getAllItems());
    }

    public void loadLibraryData(LibrarySystem system) throws IOException {
        if (!dataFilesExist()) {
            throw new IOException("CSV files not found.");
        }

        List<Member> loadedMembers = loadMembers();
        List<LibraryItem> loadedItems = loadItems(loadedMembers);

        system.clearData();
        for (Member member : loadedMembers) {
            system.addMember(member);
        }
        for (LibraryItem item : loadedItems) {
            system.addItem(item);
        }
    }

    private void saveMembers(List<Member> members) throws IOException {
        createParentDirectoryIfNeeded(membersPath);
        try (BufferedWriter writer = Files.newBufferedWriter(membersPath)) {
            writer.write("memberId,name,membershipType,borrowedCount,role,password");
            writer.newLine();

            for (Member member : members) {
                writer.write(joinCsv(
                        member.getMemberId(),
                        member.getName(),
                        membershipCodeFromStrategy(member.getMembershipStrategy()),
                        String.valueOf(member.getBorrowedCount()),
                        member.getRole(),
                        member.getPassword()
                ));
                writer.newLine();
            }
        }
    }

    private void saveItems(List<LibraryItem> items) throws IOException {
        createParentDirectoryIfNeeded(itemsPath);
        try (BufferedWriter writer = Files.newBufferedWriter(itemsPath)) {
            writer.write("itemType,itemId,title,author,isbn,price,status,returnDueDate,extra1,extra2,timesBorrowed,currentBorrowerId");
            writer.newLine();

            for (LibraryItem item : items) {
                String extra1 = "";
                String extra2 = "";

                if (item instanceof PhysicalBook physicalBook) {
                    extra1 = physicalBook.getShelflocation();
                } else if (item instanceof EBook eBook) {
                    extra1 = eBook.getDownloadUrl();
                    extra2 = String.valueOf(eBook.getFileSize());
                }

                writer.write(joinCsv(
                        item.getItemType(),
                        item.getItemId(),
                        item.getTitle(),
                        item.getAuthor(),
                        item.getIsbn(),
                        String.valueOf(item.getPrice()),
                        item.getStatus(),
                        item.getReturnDueDate() == null ? "" : item.getReturnDueDate().toString(),
                        extra1,
                        extra2,
                        String.valueOf(item.getTimesBorrowed()),
                        item.getCurrentBorrower() == null ? "" : item.getCurrentBorrower().getMemberId()
                ));
                writer.newLine();
            }
        }
    }

    private List<Member> loadMembers() throws IOException {
        List<Member> members = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(membersPath)) {
            String line = reader.readLine();
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                List<String> columns = parseCsvLine(line);
                Member member = new Member(
                        columns.get(0),
                        columns.get(1),
                        strategyFromCode(columns.get(2)),
                        Integer.parseInt(columns.get(3)),
                        columns.size() > 4 ? columns.get(4) : Member.ROLE_MEMBER,
                        columns.size() > 5 ? columns.get(5) : Member.DEFAULT_MEMBER_PASSWORD
                );
                members.add(member);
            }
        }

        return members;
    }

    private List<LibraryItem> loadItems(List<Member> members) throws IOException {
        List<LibraryItem> items = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(itemsPath)) {
            String line = reader.readLine();
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                List<String> columns = parseCsvLine(line);
                String itemType = columns.get(0);
                LibraryItem item;

                if ("PhysicalBook".equalsIgnoreCase(itemType)) {
                    item = new PhysicalBook(
                            columns.get(1),
                            columns.get(2),
                            columns.get(3),
                            columns.get(4),
                            Double.parseDouble(columns.get(5)),
                            columns.get(8),
                            columns.get(6)
                    );
                } else {
                    double fileSize = columns.get(9).isEmpty() ? 0.0 : Double.parseDouble(columns.get(9));
                    item = new EBook(
                            columns.get(1),
                            columns.get(2),
                            columns.get(3),
                            columns.get(4),
                            Double.parseDouble(columns.get(5)),
                            columns.get(8),
                            fileSize,
                            columns.get(6)
                    );
                }

                item.setTimesBorrowed(Integer.parseInt(columns.get(10)));
                if (!columns.get(11).isEmpty()) {
                    Member borrower = findMember(columns.get(11), members);
                    LocalDate dueDate = columns.get(7).isEmpty() ? null : LocalDate.parse(columns.get(7));
                    item.restoreBorrowState(borrower, dueDate);
                }

                items.add(item);
            }
        }

        return items;
    }

    private Member findMember(String memberId, List<Member> members) {
        for (Member member : members) {
            if (member.getMemberId().equalsIgnoreCase(memberId)) {
                return member;
            }
        }
        return null;
    }

    private MembershipStrategy strategyFromCode(String code) {
        if ("STUDENT".equalsIgnoreCase(code)) {
            return new StudentMembershipStrategy();
        }
        if ("PREMIUM".equalsIgnoreCase(code)) {
            return new PremiumMembershipStrategy();
        }
        return new BasicMembershipStrategy();
    }

    private String membershipCodeFromStrategy(MembershipStrategy strategy) {
        if (strategy instanceof StudentMembershipStrategy) {
            return "STUDENT";
        }
        if (strategy instanceof PremiumMembershipStrategy) {
            return "PREMIUM";
        }
        return "BASIC";
    }

    private String joinCsv(String... values) {
        StringBuilder builder = new StringBuilder();
        for (int index = 0; index < values.length; index++) {
            if (index > 0) {
                builder.append(',');
            }
            builder.append(escapeCsv(values[index]));
        }
        return builder.toString();
    }

    private String escapeCsv(String value) {
        String safeValue = value == null ? "" : value;
        if (safeValue.contains(",") || safeValue.contains("\"")) {
            return "\"" + safeValue.replace("\"", "\"\"") + "\"";
        }
        return safeValue;
    }

    private List<String> parseCsvLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int index = 0; index < line.length(); index++) {
            char currentChar = line.charAt(index);
            if (currentChar == '"') {
                if (inQuotes && index + 1 < line.length() && line.charAt(index + 1) == '"') {
                    current.append('"');
                    index++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (currentChar == ',' && !inQuotes) {
                values.add(current.toString());
                current.setLength(0);
            } else {
                current.append(currentChar);
            }
        }

        values.add(current.toString());
        return values;
    }

    private void createParentDirectoryIfNeeded(Path path) throws IOException {
        Path parent = path.getParent();
        if (parent != null && Files.notExists(parent)) {
            Files.createDirectories(parent);
        }
    }
}
