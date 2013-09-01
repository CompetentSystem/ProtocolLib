package com.comphenix.protocol.wrappers;

import static org.junit.Assert.*;

import java.util.List;

import net.minecraft.server.v1_6_R2.AttributeModifier;
import net.minecraft.server.v1_6_R2.AttributeSnapshot;
import net.minecraft.server.v1_6_R2.Packet44UpdateAttributes;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.Packets;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedAttributeModifier.Operation;
import com.google.common.collect.Lists;

public class WrappedAttributeTest {
	private WrappedAttributeModifier doubleModifier;
	private WrappedAttributeModifier constantModifier;
	private WrappedAttribute attribute;
	
	@BeforeClass
	public static void initializeBukkit() throws IllegalAccessException {
		BukkitInitialization.initializePackage();
	}

	@Before
	public void setUp() {
		// Create a couple of modifiers
		doubleModifier = 
			WrappedAttributeModifier.newBuilder().
			name("Double Damage").
			amount(1).
			operation(Operation.ADD_PERCENTAGE).
			build();
		constantModifier = 
			WrappedAttributeModifier.newBuilder().
			name("Damage Bonus").
			amount(5).
			operation(Operation.ADD_NUMBER).
			build();

		// Create attribute
		attribute = WrappedAttribute.newBuilder().
				attributeKey("generic.attackDamage").
				baseValue(2).
				packet(new PacketContainer(Packets.Server.UPDATE_ATTRIBUTES)).
				modifiers(Lists.newArrayList(constantModifier, doubleModifier)).
				build();
	}
	
	@Test
	public void testEquality() {
		// Check wrapped equality
		assertEquals(doubleModifier, doubleModifier);
		assertNotSame(constantModifier, doubleModifier);
		
		assertEquals(doubleModifier.getHandle(), getModifierCopy(doubleModifier));
		assertEquals(constantModifier.getHandle(), getModifierCopy(constantModifier));
	}
	
	@Test
	public void testAttribute() {
		assertEquals(attribute, WrappedAttribute.fromHandle(getAttributeCopy(attribute)));
		
		assertTrue(attribute.hasModifier(doubleModifier.getUUID()));
		assertTrue(attribute.hasModifier(constantModifier.getUUID()));
	}
	
	/**
	 * Retrieve the equivalent NMS attribute.
	 * @param attribute - the wrapped attribute.
	 * @return The equivalent NMS attribute.
	 */
	private AttributeSnapshot getAttributeCopy(WrappedAttribute attribute) {
		List<AttributeModifier> modifiers = Lists.newArrayList();
		
		for (WrappedAttributeModifier wrapper : attribute.getModifiers()) {
			modifiers.add((AttributeModifier) wrapper.getHandle());
		}
		return new AttributeSnapshot(
			(Packet44UpdateAttributes) attribute.getParentPacket().getHandle(), 
			attribute.getAttributeKey(), attribute.getBaseValue(), modifiers);
	}
	
	private AttributeModifier getModifierCopy(WrappedAttributeModifier modifier) {
		return new AttributeModifier(modifier.getUUID(), modifier.getName(), modifier.getAmount(), modifier.getOperation().getId());
	}
}