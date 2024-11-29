package net.optifine.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import net.optifine.render.VboRange;

public class LinkedListTest {
   public static void main(String[] args) throws Exception {
      LinkedList<VboRange> linkedlist = new LinkedList<>();
      List<VboRange> list = new ArrayList<>();
      List<VboRange> list1 = new ArrayList<>();
      Random random = new Random();
      int i = 100;

      for(int j = 0; j < i; ++j) {
         VboRange vborange = new VboRange();
         vborange.setPosition(j);
         list.add(vborange);
      }

      for(int k = 0; k < 100000; ++k) {
         checkLists(list, list1, i);
         checkLinkedList(linkedlist, list1.size());
         if (k % 5 == 0) {
            dbgLinkedList(linkedlist);
         }

         if (random.nextBoolean()) {
            if (!list.isEmpty()) {
               VboRange vborange2 = list.get(random.nextInt(list.size()));
               LinkedList.Node<VboRange> node = vborange2.getNode();
               if (random.nextBoolean()) {
                  linkedlist.addFirst(node);
                  dbg("Add first: " + vborange2.getPosition());
               } else if (random.nextBoolean()) {
                  linkedlist.addLast(node);
                  dbg("Add last: " + vborange2.getPosition());
               } else {
                  if (list1.isEmpty()) {
                     continue;
                  }

                  VboRange vborange1 = list1.get(random.nextInt(list1.size()));
                  LinkedList.Node<VboRange> node1 = vborange1.getNode();
                  linkedlist.addAfter(node1, node);
                  dbg("Add after: " + vborange1.getPosition() + ", " + vborange2.getPosition());
               }

               list.remove(vborange2);
               list1.add(vborange2);
            }
         } else if (!list1.isEmpty()) {
            VboRange vborange3 = list1.get(random.nextInt(list1.size()));
            LinkedList.Node<VboRange> node2 = vborange3.getNode();
            linkedlist.remove(node2);
            dbg("Remove: " + vborange3.getPosition());
            list1.remove(vborange3);
            list.add(vborange3);
         }
      }

   }

   private static void dbgLinkedList(LinkedList<VboRange> linkedList) {
      StringBuffer sb = new StringBuffer();
      Iterator<LinkedList.Node<VboRange>> it = linkedList.iterator();
      while (it.hasNext()) {
         LinkedList.Node<VboRange> node = it.next();
         VboRange range = node.getItem();
         if (sb.length() > 0) {
            sb.append(", ");
         }
         sb.append(range.getPosition());
      }
      LinkedListTest.dbg("List: " + sb);
   }

   private static void checkLinkedList(LinkedList<VboRange> linkedList, int used) {
      if (linkedList.getSize() != used) {
         throw new RuntimeException("Wrong size, linked: " + linkedList.getSize() + ", used: " + used);
      } else {
         int i = 0;

         for(LinkedList.Node<VboRange> node = linkedList.getFirst(); node != null; node = node.getNext()) {
            ++i;
         }

         if (linkedList.getSize() != i) {
            throw new RuntimeException("Wrong count, linked: " + linkedList.getSize() + ", count: " + i);
         } else {
            int j = 0;

            for(LinkedList.Node<VboRange> node1 = linkedList.getLast(); node1 != null; node1 = node1.getPrev()) {
               ++j;
            }

            if (linkedList.getSize() != j) {
               throw new RuntimeException("Wrong count back, linked: " + linkedList.getSize() + ", count: " + j);
            }
         }
      }
   }

   private static void checkLists(List<VboRange> listFree, List<VboRange> listUsed, int count) {
      int i = listFree.size() + listUsed.size();
      if (i != count) {
         throw new RuntimeException("Total size: " + i);
      }
   }

   private static void dbg(String str) {
      System.out.println(str);
   }
}
