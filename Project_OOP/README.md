# Library Management System

## Overview
This is a menu-driven Java library system in `package Project_OOP`.

The program supports:
- login and member registration
- member management
- library item management
- borrowing and returning
- overdue fine calculation
- reports
- CSV save/load
- auto-save after add, update, delete, borrow, and return
- duplicate checking for member and item data

## OOP Concepts Used
- `class`: `Member`, `LibrarySystem`, `LibraryFileManager`, `InputHelper`
- `inheritance`: `PhysicalBook` and `EBook` extend `LibraryItem`
- `interface`: `Taxable`, `DigitalContent`, `MembershipStrategy`
- `overriding`: `displayDetails()`, `calculateLateFee()`, `calculateTax()`
- `overloading`:
  - `Member` constructors
  - `EBook` constructors
  - `LibraryItem` constructors
  - `LibrarySystem.findItems(String)` and `LibrarySystem.findItems(String, boolean)`

## Main Features
1. Login and register member accounts
2. Show all members
3. Show all library items
4. Add member with auto-generated member ID
5. Add item with auto-generated item ID
6. Search member or item
7. Update member or item
8. Delete member or item
9. Borrow item with availability and borrow-limit check
10. Return item with late-fee calculation
11. Reports:
   - overdue items
   - most borrowed items
   - price and tax report
12. Save data to CSV
13. Load data from CSV

## Use Cases
1. Register a new member
2. Add a new physical book
3. Add a new e-book
4. Search for an item by keyword
5. Borrow an available item
6. Return an item and calculate late fee
7. Update member membership type
8. Delete an item that is not borrowed
9. Save current data to CSV
10. Load previous data from CSV

## File Storage
The system stores data in memory during runtime and can save/load these files:
- `src/Project_OOP/library_members.csv`
- `src/Project_OOP/library_items.csv`

## Class Structure
- `LibraryManagementApp`: main menu and user interaction
- `LibrarySystem`: singleton service that stores members and items in memory
- `LibraryItem`: abstract base class for all media
- `PhysicalBook`: physical media with shelf location
- `EBook`: digital media with download URL and file size
- `Member`: library member with strategy-based membership behavior
- `MembershipStrategy` and implementations:
  - `BasicMembershipStrategy`
  - `StudentMembershipStrategy`
  - `PremiumMembershipStrategy`
- `LibraryFileManager`: CSV persistence
- `InputHelper`: input validation helper

## How To Run
Compile from the project root:

```bash
javac -d out src/Project_OOP/*.java
```

Run:

```bash
java -cp out Project_OOP.LibraryManagementApp
```

## Notes
- If CSV files are not found, the program loads sample data into memory.
- Fixed librarian account:
  - Name: `Librarian`
  - Password: `admin123`
- Only the librarian can use `Search member/item`, `Update member/item`, and `Delete member/item`.
- New members receive an auto-increment ID such as `M004`, `M005`, and so on.
- New items receive an auto-increment ID such as `B004` for books and `E003` for e-books.
- Important actions automatically save to CSV immediately.
- The system prevents duplicate members by name and duplicate items by ISBN or title plus author.
- Borrowed items cannot be deleted.
- Members with borrowed items cannot be deleted.
- `PhysicalBook` has a simple late fee of `5 Baht` per overdue day.
- `EBook` has `0 Baht` late fee.
